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

    @BeforeClass
    public static void init() {

        //Security issue: it's dangerous to pass projectId and APIKey in url string, as it is be logged somewhere
        token = Requests.retrieveToken(API_KEY, PROJECT_ID);
    }

    @Test
    public void runJobBasicFlow() {

        //UX Bug -  Workflow name is not unique

        //There is an error in Challenge description - :1 is missing for tasks names in examples jsons

        String workflowId = Requests.createWorkflow(token, PROJECT_ID, "{ \"name\": \"QA Challenge\",\"description\": \"QA Challenge\"}");
        Response getWorkflow = Requests.getWorkflowInfo(token, PROJECT_ID, workflowId);
        assertTrue(getWorkflow.jsonPath().get("data.id").equals(workflowId) && getWorkflow.jsonPath().get("data.name").equals("QA Challenge"));

        //Note:  works and tested also with PUT
        Response configResponse = Requests.configWorkflow(token, PROJECT_ID, workflowId, Utils.resourceAsString("workflowConfig.json"));
        assertTrue(configResponse.jsonPath().getString("data[0].name").equals("sobloo-s2-l1c-aoiclipped:1")
                && configResponse.jsonPath().getString("data[1].name").equals("sharpening:1"));

        String jobId = Requests.createRunJob(token, PROJECT_ID, workflowId, Utils.resourceAsString("job.json"));

        with().pollInterval(500, TimeUnit.MILLISECONDS).await().atMost(120, TimeUnit.SECONDS)
                .until(() -> Requests.getJobStatus(token, PROJECT_ID, jobId).equals("SUCCEEDED"));

        Requests.deleteWorkflow(token, PROJECT_ID, workflowId);
        assertTrue(Requests.getWorkflowInfo(token, PROJECT_ID, workflowId).getStatusCode() == 404);


    }


}
