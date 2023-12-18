package zdn.springframework.spring6restmvc.funtionalTests;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class GetBeer {

    private static final String BASE_URL = "http://localhost:8080";

    private static final String BEER_PATH = "/api/v1/beer";

    String accessToken;

    @BeforeEach
    void setUp() {
        Response response = given()
                .baseUri("http://localhost:9000")
                .header("Authorization", "Basic b2lkYy1jbGllbnQ6c2VjcmV0")
                .formParams(
                        "grant_type", "client_credentials",
                        "scope", "message.read message.write"
                )
                .contentType(ContentType.fromContentType("application/x-www-form-urlencoded"))
                .when()
                .post("/oauth2/token");

        response.then()
                .statusCode(200);

        JsonPath jsonPath = new JsonPath(response.getBody().asString());
        accessToken = jsonPath.getString("access_token");
    }

    @Test
    void statusCode200() {
        Response response = given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .when()
                .get(BEER_PATH);

        response.then()
                .statusCode(200);
    }


}
