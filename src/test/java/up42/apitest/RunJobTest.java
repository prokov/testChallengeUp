package up42.apitest;

import com.jayway.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import up42.apitest.utils.Requests;
import up42.apitest.utils.Utils;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
//import static up42.apitest.Constants.API_KEY;
//import static up42.apitest.Constants.PROJECT_ID;

@Slf4j
public class RunJobTest {

    //TODO Parametrize all

    private static String token;
    private final String name = "TestWorkFlow";
    private final String block1 = "sobloo-s2-l1c-aoiclipped";
    private final String block2 = "sharpening";
    private static String  apiKey;
    private static String  projectId;



    @BeforeClass
    public static void init() {

        apiKey= System.getenv("apiKey");
        projectId = System.getenv("projectId");

        //Security issue: it's dangerous to pass projectId and APIKey in url string, as it is logged somewhere
      //  token = Requests.retrieveToken(API_KEY, PROJECT_ID);

        token = Requests.retrieveToken(apiKey, projectId);
    }

    @Test

    //TODO To parametrize test for other blocks/jobs
    public void runJobBasicFlow() throws JSONException {

        //UX Bug -  Workflow name is not unique

        //TODO JSON assert for all steps
        //TODO delete in case of failure
        //TODO  Add check  for simultaneous jobs {"data":null,"error":{"code":429,"message":"Your project has too many non-finished jobs","details":null}}

        Response createResponse = Requests.createWorkflow(token, projectId, "{\"name\":\"" + name + "\"}");

      try {
          JSONAssert.assertEquals("Create  Workflow response is invalid",
                  Utils.resourceAsString("expected/createWF.json"), createResponse.asString(),
                  new CustomComparator(JSONCompareMode.STRICT
//                        new Customization("data.createdAt", (o1, o2) -> true),
//                        new Customization("data.id", (o1, o2) -> true),
//                        new Customization("data.updatedAt", (o1, o2) -> true)
                  ));
      }
      catch (AssertionError e){

          log.error("Assertion error{}", createResponse.asString());
          fail();
      }



        String workflowId = createResponse.jsonPath().getString("data.id");
        Response getWorkflow = Requests.getWorkflowInfo(token, projectId, workflowId);
//        assertTrue(getWorkflow.jsonPath().get("data.id").equals(workflowId)
//                && getWorkflow.jsonPath().get("data.name").equals(name));


        //Note:  works and tested also with PUT

        Response configResponse = Requests.configWorkflow(token, projectId, workflowId,
                Utils.resourceAsString("workflowConfig.json"));


        assertTrue(configResponse.jsonPath().getString("data[0].blockName").equals(block1)
                && configResponse.jsonPath().getString("data[1].blockName").equals(block2));


        String jobId = Requests.createRunJob(token, projectId, workflowId, Utils.resourceAsString("job.json"));

        with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(120, TimeUnit.SECONDS)
                .until(() -> Requests.getJobStatus(token, projectId, jobId).equals("SUCCEEDED"));

        Requests.deleteWorkflow(token, projectId, workflowId);
        assertTrue(Requests.getWorkflowInfo(token, projectId, workflowId).getStatusCode() == 404);


    }


}
