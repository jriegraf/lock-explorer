package de.riegraf.lockexplorer.utils;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class Utils {

  public static Object throwIfNull(Object o, Supplier<Exception> supplier) throws Exception {
    if (o == null) {
      throw supplier.get();
    } else {
      return o;
    }
  }

  public static Object throwIfNull(Object o) throws Exception {
    return throwIfNull(o, NoSuchElementException::new);
  }
}
