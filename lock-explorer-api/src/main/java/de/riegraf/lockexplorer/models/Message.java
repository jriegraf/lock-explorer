package de.riegraf.lockexplorer.models;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.String.format;

@RequiredArgsConstructor
@Getter
@Setter
public class Message {

  @NonNull
  final MessageType type;
  final Optional<String> user;
  final Optional<Map<String, Object>> payload;

  public Object getPayloadValue(String key) {
    if (payload.isEmpty()) throw new NoSuchElementException("Message has no payload.");
    Object o = payload.get().get(key);
    if (o == null) throw new NoSuchElementException(format("No '%s' in payload.", key));
    return o;
  }

  @Override
  public String toString() {
    return "Message{" +
        "type=" + type +
        ", user=" + user +
        ", payload='" + payload + '\'' +
        '}';
  }

  public enum MessageType {
    REGISTER,
    GET_SESSIONS,
    OPEN_SESSION,
    CLOSE_SESSION,
    EXECUTE_SQL,
    GET_TABLE,
    GET_AVAILABLE_TABLES,
    GET_VIEW,
    GET_AVAILABLE_VIEWS,
  }
}
