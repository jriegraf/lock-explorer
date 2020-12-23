package de.riegraf.lockexplorer.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KeyValueTuple {
  private final String key;
  private final Object value;
}
