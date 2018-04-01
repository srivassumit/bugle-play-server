package controllers;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

import models.Events;
import models.Users;
import play.Logger;
import play.Logger.ALogger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import services.DatabaseService;
import util.Strings;

/**
 * This controller contains an action to handle HTTP requests to the
 * application's home page.
 */
@Singleton
public class HomeController extends Controller {

	private final DatabaseService databaseService;
	private static final ALogger LOG = Logger.of(HomeController.class);

	@Inject
	public HomeController(DatabaseService databaseService) {
		this.databaseService = databaseService;
	}

	/**
	 * An action that renders an HTML page with a welcome message. The configuration
	 * in the <code>routes</code> file means that this method will be called when
	 * the application receives a <code>GET</code> request with a path of
	 * <code>/</code>.
	 */
	public Result index() {
		return ok(views.html.index.render());
	}

	public Result getOrganizations() {
		LOG.debug("getOrganizations method called.");
		List<Users> organizations = databaseService.getOrganizations();
		return ok(createSuccessResponse("organizations", new Gson().toJson(organizations)))
				.withHeader("Access-Control-Allow-Origin", "*");
	}

	public Result getEvents(String orgId) {
		LOG.debug("getEvents method called.");
		List<Events> events = databaseService.getEvents(Integer.valueOf(orgId));
		return ok(createSuccessResponse("events", new Gson().toJson(events))).withHeader("Access-Control-Allow-Origin",
				"*");
	}

	/**
	 * Creates a new Event. Sample JSON to create event:
	 * 
	 * <pre>
	 * {
	 * "e_name":"Event 1",
	 * "location":"Hunt Library",
	 * "datetime":"04.14.2018 11:00AM",
	 * "description":"Volunteering Event at Hunt Library",
	 * "members":"12",
	 * "u_id":"4",
	 * "status":"active"
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public Result createEvent() {
		LOG.debug("createEvent method called.");
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data for event.").withHeader("Access-Control-Allow-Origin", "*");
		} else {
			String name = json.findPath("e_name").textValue();
			String location = json.findPath("location").textValue();
			String datetime = json.findPath("datetime").textValue();
			String description = json.findPath("description").textValue();
			String members = json.findPath("members").textValue();
			int uId = json.findPath("u_id").intValue();
			String status = json.findPath("status").textValue();

			Events event = new Events(name, location, datetime, description, members, uId, status);

			// save to DB and return response.
			if (databaseService.insertEvent(event)) {
				return ok(createSuccessResponse(Strings.MESSAGE, "Event Created"))
						.withHeader("Access-Control-Allow-Origin", "*");
			} else {
				return ok(createErrorResponse(Strings.FAIL)).withHeader("Access-Control-Allow-Origin", "*");
			}
		}
	}

	/**
	 * Method to validate user login
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public Result validateLogin() {
		LOG.debug("validateLogin method called.");
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data for login.").withHeader("Access-Control-Allow-Origin", "*");
		} else {
			String email = json.findPath("email").textValue();
			String password = json.findPath("password").textValue();

			// validate from DB and return response.
			Users loginUser = databaseService.validateLogin(email, password);
			if (loginUser != null) {
				return ok(createSuccessResponse(Strings.USER, new Gson().toJson(loginUser)))
						.withHeader("Access-Control-Allow-Origin", "*");
			} else {
				return ok(createErrorResponse(Strings.LOGIN_FAIL)).withHeader("Access-Control-Allow-Origin", "*");
			}
		}
	}

	/**
	 * Creates new user. Sample JSON to create new user:
	 * 
	 * <pre>
	 * {
	 * "u_name":"Harry",
	 * "email":"harry@gmail.com",
	 * "mobile":"1234567890",
	 * "dob":"06.11.94",
	 * "password":"harrioscar",
	 * "type":"vol"
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	public Result createUser() {
		LOG.debug("createUser method called.");
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data for user.").withHeader("Access-Control-Allow-Origin", "*");
		} else {

			String name = json.findPath("u_name").textValue();
			String email = json.findPath("email").textValue();
			String mobile = json.findPath("mobile").textValue();
			String dob = json.findPath("dob").textValue();
			String password = json.findPath("password").textValue();
			String type = json.findPath("type").textValue();
			String description = json.findPath("description").textValue();
			String location = json.findPath("location").textValue();
			String website = json.findPath("website").textValue();

			Users user = new Users(name, email, mobile, dob, password, type, description, location, website);

			// save to DB and return response.
			if (databaseService.insertUser(user)) {
				return ok(createSuccessResponse(Strings.USER, new Gson().toJson(user)))
						.withHeader("Access-Control-Allow-Origin", "*");
			} else {
				return ok(createErrorResponse(Strings.FAIL)).withHeader("Access-Control-Allow-Origin", "*");
			}
		}
	}

	/**
	 * 
	 * @param vId
	 * @return
	 */
	public Result getVolunteerEvents(String vId) {
		LOG.debug("getVolunteerEvents method called.");
		return ok(createSuccessResponse(Strings.MESSAGE, "Unimplemented method"))
				.withHeader("Access-Control-Allow-Origin", "*");
	}

	/**
	 * 
	 * @param eId
	 * @return
	 */
	public Result getEventVolunteers(String eId) {
		LOG.debug("getEventVolunteers method called.");
		return ok(createSuccessResponse(Strings.MESSAGE, "Unimplemented method"))
				.withHeader("Access-Control-Allow-Origin", "*");
	}

	/**
	 * This method returns the following JSON response:
	 * 
	 * <pre>
	 * {
	 *   "status":"success",
	 *   "&lt;key&gt;":"&lt;message&gt;"
	 * </pre>
	 * 
	 * @param key
	 *            the key
	 * @param message
	 *            the message
	 * @return
	 */
	private ObjectNode createSuccessResponse(String key, Object message) {
		ObjectNode result = Json.newObject();
		result.put(Strings.STATUS, Strings.SUCCESS);
		if (key != null && message != null) {
			result.put(key, (String) message);
		}
		return result;
	}

	/**
	 * This method returns the following JSON response:
	 * 
	 * <pre>
	 * {
	 *   "status":"error",
	 *   "message":"&lt;message&gt;"
	 * </pre>
	 * 
	 * @param message
	 *            the message
	 * @return
	 */
	private ObjectNode createErrorResponse(String message) {
		ObjectNode result = Json.newObject();
		result.put(Strings.STATUS, Strings.ERROR);
		result.put(Strings.MESSAGE, message);
		return result;
	}

}
