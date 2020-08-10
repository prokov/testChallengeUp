package up42.apitest.utils;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import static com.jayway.restassured.RestAssured.given;
import static up42.apitest.Constants.BASE_URL;
import static up42.apitest.Constants.TOKEN_ENDPOINT;


@Slf4j
public class Requests {


    public static String retrieveToken(String apiKey, String projectId) {

        String body = "grant_type=client_credentials";
        Response response =
                given()
                        .contentType(ContentType.URLENC)
                        .body(body)
                        .when()
                        .post("https://" + projectId + ":" + apiKey + "@" + BASE_URL + TOKEN_ENDPOINT)
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();


       // System.out.println("Response " + response.asString());

        log.info("Retrieve token response: {}", response.asString() );
        return response.jsonPath().getString("data.accessToken");

    }

    public static String createRunJob(String token, String projectId, String workflowId, String job) {

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(job)
                        .when()
                        .post("https://" + BASE_URL + "/projects/" + projectId + "/workflows/" + workflowId + "/jobs")
                        .then()
                        // .statusCode(200)
                        .extract()
                        .response();
        System.out.println("Create/Run job response " + response.asString());
        return response.jsonPath().getString("data.id");
    }

    public static String getJobStatus(String token, String projectId, String jobId) {
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("https://" + BASE_URL + "/projects/" + projectId + "/jobs/" + jobId)
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();
        System.out.println("Get job status response " + response.jsonPath().getString("data.status"));
        return response.jsonPath().getString("data.status");
    }

    public static Response getWorkflowInfo(String token, String projectId, String workflowId) {
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("https://" + BASE_URL + "/projects/" + projectId + "/workflows/" + workflowId)
                        .then()
                        .extract()
                        .response();


        System.out.println("Get workflow response " + response.asString());
        return response;
    }


    public static Response createWorkflow(String token, String projectId, String body) {

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .post("https://" + BASE_URL + "/projects/" + projectId + "/workflows/")
                        .then()
                        // .statusCode(200)
                        .extract()
                        .response();
        System.out.println("Create workflow response " + response.asString());
        return response;
    }

    public static Response configWorkflow(String token, String projectId,
                                          String workflowId, String body) {

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(body)
                        .when()
                        .post("https://" + BASE_URL + "/projects/" + projectId + "/workflows/"
                                + workflowId + "/tasks/")
                        //   .put("https://" + BASE_URL + "/projects/" + projectId + "/workflows/" + workflowId)
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();
        System.out.println("Config  workflow response " + response.asString());
        return response;
    }

    public static Response deleteWorkflow(String token, String projectId, String workflowId) {

        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("https://" + BASE_URL + "/projects/" + projectId + "/workflows/" + workflowId)
                .then()
                .statusCode(204)
                .extract()
                .response();

    }

}




