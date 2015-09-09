package org.gusdb.fgputil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

public class FormatUtil {

  public static final String NL = System.lineSeparator();
  public static final String TAB = "\t";
  public static final String UTF8_ENCODING = "UTF-8";
  
  private FormatUtil() {}

  @SuppressWarnings("serial")
  private static class CurrentStackTrace extends Throwable { }
  
  public static String getCurrentStackTrace() {
    return getStackTrace(new CurrentStackTrace());
  }

  public static String getStackTrace(Throwable t) {
    StringWriter str = new StringWriter(150);
    t.printStackTrace(new PrintWriter(str));
    return str.toString();
  }

  public static String getUtf8EncodedString(String s) {
    try {
      return URLEncoder.encode(s, UTF8_ENCODING);
    }
    catch (UnsupportedEncodingException e) {
      // this should never happen; if it does, wrap in RuntimeException
      throw new RuntimeException(UTF8_ENCODING + " encoding no longer supported by Java.", e);
    }
  }
  
  public static String splitCamelCase(String s) {
    return s.replaceAll(
            String.format("%s|%s|%s",
                      "(?<=[A-Z])(?=[A-Z][a-z])",
                      "(?<=[^A-Z])(?=[A-Z])",
                      "(?<=[A-Za-z])(?=[^A-Za-z])"),
            " ");
  }

  public static String multiLineFormat(String str, int maxCharsPerLine) {
    String[] tokens = str.split(" ");
    String newS = "";
    int lineTotal = 0;
    for (int curTok = 0; curTok < tokens.length; curTok++) {
      if (newS.equals("") || // never add newline before first token
          lineTotal + 1 + tokens[curTok].length() <= maxCharsPerLine ||
          lineTotal == 0 && tokens[curTok].length() > maxCharsPerLine) {
        // add this token to the current line
      }
      else {
        // start new line
        newS += "\\n";
        lineTotal = 0;
      }
      lineTotal += (lineTotal == 0 ? 0 : 1) + tokens[curTok].length();
      newS += (lineTotal == 0 ? "" : " ") + tokens[curTok];
    }
    return newS;
  }

  public static String join(Object[] array, String delim) {
    if (array == null || array.length == 0) return "";
    StringBuilder sb = new StringBuilder();
    sb.append(array[0] == null ? "null" : array[0].toString());
    for (int i = 1; i < array.length; i++) {
      sb.append(delim).append(array[i] == null ? "null" : array[i].toString());
    }
    return sb.toString();
  }
  
  public static String arrayToString(Object[] array) {
    return arrayToString(array, ", ");
  }

  public static String arrayToString(Object[] array, String delim) {
    if (array == null) return "null";
    StringBuilder sb = new StringBuilder("[ ");
    sb.append(join(array, delim));
    return sb.append(" ]").toString();
  }

  public static String printArray(String[] array) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (String s : array) {
        if (sb.length() > 1) sb.append(", ");
        sb.append("\"" + s + "\"");
    }
    sb.append("}");
    return sb.toString();
  }

  public static String printArray(String[][] array) {
    String newline = System.getProperty("line.separator");
    StringBuilder sb = new StringBuilder();
    for (String[] parts : array) {
        sb.append(printArray(parts));
        sb.append(newline);
    }
    return sb.toString();
  }

  public static String getCamelCaseDisplayVal(String str) {
    StringBuilder newStr = new StringBuilder();
    boolean justSawSpace = true; // set so first char is upper case
    str = str.trim();
    for (int i=0; i < str.length(); i++) {
      char thisChar = str.charAt(i);
      if (thisChar == ' ' || thisChar == '_' || thisChar == '-') {
        if (!justSawSpace) { // only do a single whitespace char
          newStr.append(' ');
          justSawSpace = true;
        }
      } else if (justSawSpace) {
        newStr.append(String.valueOf(thisChar).toUpperCase());
        justSawSpace = false;
      } else {
        newStr.append(String.valueOf(thisChar).toLowerCase());
      }
    }
    return newStr.toString();
  }
  
  public static boolean isInteger(String s) {
    try { Integer.parseInt(s); return true; }
    catch (NumberFormatException e) { return false; }
  }

  public static enum Style {
    SINGLE_LINE(" ", "", ", ", " "),
    MULTI_LINE(NL, "   ", ","+NL, NL);
    
    public final String introDelimiter;
    public final String recordIndent;
    public final String mapArrow = " => ";
    public final String recordDelimiter;
    public final String endDelimiter;
    
    private Style(String id, String ri, String rd, String ed) {
      introDelimiter = id; recordIndent = ri;
      recordDelimiter = rd; endDelimiter = ed;
    }
  }
  
  public static <S,T> String prettyPrint(Map<S,T> map) {
    return prettyPrint(map, Style.SINGLE_LINE);
  }
  
  public static <S,T> String prettyPrint(Map<S,T> map, Style style) {
    StringBuilder sb = new StringBuilder("{").append(style.introDelimiter);
    boolean firstRecord = true;
    for (Entry<S,T> entry : map.entrySet()) {
      sb.append(firstRecord ? "" : style.recordDelimiter).append(style.recordIndent)
        .append(entry.getKey().toString()).append(style.mapArrow)
        .append(entry.getValue() == null ? null : entry.getValue().toString());
      firstRecord = false;
    }
    return sb.append(style.endDelimiter).append("}")
             .append(style.endDelimiter).toString();
  }

  public static String getPctFromRatio(long numerator, long denominator) {
    Double ratio = (double)numerator / (double)denominator;
    return new DecimalFormat("##0.0").format(ratio * 100D) + "%";
  }

  public static String paramsToString(Map<String, String[]> parameters) {
    StringBuilder str = new StringBuilder("{" + NL);
    for (Entry<String, String[]> param : parameters.entrySet()) {
      str.append("   ").append(param.getKey()).append(": ").append(arrayToString(param.getValue())).append(NL);
    }
    return str.append("}").append(NL).toString();
  }

}
