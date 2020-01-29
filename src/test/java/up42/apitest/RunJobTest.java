package up42.apitest;

import com.jayway.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import up42.apitest.utils.Requests;
import up42.apitest.utils.Utils;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;
import static org.junit.Assert.assertTrue;
import static up42.apitest.Constants.API_KEY;
import static up42.apitest.Constants.PROJECT_ID;

public class RunJobTest {

    private static String token;
    private final String name = "TestWorkFlow";
    private final String block1 = "sobloo-s2-l1c-aoiclipped";
    private final String block2 = "sharpening";


    @BeforeClass
    public static void init() {

        //Security issue: it's dangerous to pass projectId and APIKey in url string, as it is logged somewhere
        token = Requests.retrieveToken(API_KEY, PROJECT_ID);
    }

    @Test

    //TODO To parametrize test for other blocks/jobs
    public void runJobBasicFlow() {

        //UX Bug -  Workflow name is not unique

        String workflowId = Requests.createWorkflow(token, PROJECT_ID, "{\"name\":\"" + name + "\"}");
        Response getWorkflow = Requests.getWorkflowInfo(token, PROJECT_ID, workflowId);
        assertTrue(getWorkflow.jsonPath().get("data.id").equals(workflowId)
                && getWorkflow.jsonPath().get("data.name").equals(name));

        //Note:  works and tested also with PUT

        Response configResponse = Requests.configWorkflow(token, PROJECT_ID, workflowId,
                Utils.resourceAsString("workflowConfig.json"));


        assertTrue(configResponse.jsonPath().getString("data[0].blockName").equals(block1)
                && configResponse.jsonPath().getString("data[1].blockName").equals(block2));


        String jobId = Requests.createRunJob(token, PROJECT_ID, workflowId, Utils.resourceAsString("job.json"));

        with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(120, TimeUnit.SECONDS)
                .until(() -> Requests.getJobStatus(token, PROJECT_ID, jobId).equals("SUCCEEDED"));

        Requests.deleteWorkflow(token, PROJECT_ID, workflowId);
        assertTrue(Requests.getWorkflowInfo(token, PROJECT_ID, workflowId).getStatusCode() == 404);


    }


}
