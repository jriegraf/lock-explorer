package de.riegraf.lockexplorer.models;

import de.riegraf.lockexplorer.utils.KeyValueTuple;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Response {

  private HttpStatus status;
  private Map payload;

  public Response(HttpStatus status, Map<String, Object> payload) {
    super();
    this.status = status;
    this.payload = payload;
  }

  public static ResponseEntity<Response> create(HttpStatus status, Map<String, Object> payload) {
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
        .body(new Response(status, payload));
  }

  public static ResponseEntity<Response> createError(HttpStatus status, String message) {
    Map<String, Object> payload = new HashMap<>(1);
    payload.put("Message", message);
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
        .body(new Response(status, payload));
  }

  public static ResponseEntity<Response> ok(Map<String, Object> result) {
    return create(HttpStatus.OK, result);
  }

  public static ResponseEntity<Response> ok(KeyValueTuple tuple) {
    Map<String, Object> map = new HashMap<>(1);
    map.put(tuple.getKey(), tuple.getValue());
    return ok(map);
  }
}