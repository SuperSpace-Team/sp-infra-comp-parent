package com.sp.infra.comp.logger.log;

/**
 * 异常堆栈处理日志工具类
 */
public class ThrowableStackLogger {
  /**
   * 记录组件错误/异常
   * @param LT
   * @param e
   */
  public static void logCompError(String LT, Throwable e) {
    StringBuilder stackTraces = new StringBuilder();
    recursivelyCause(e, stackTraces);
    CompLogger.error(LT, stackTraces.toString());
  }

  /**
   * 递归异常堆栈
   * @param e
   * @param stackTraces
   */
  private static void recursivelyCause(Throwable e, StringBuilder stackTraces) {
    if (e == null) {
      return;
    }
    StackTraceElement[] stackTrace = e.getStackTrace();
    if (stackTrace == null) {
      return;
    }
    stackTraces.append("\r\n\tCaused by : " + e.getClass().getName() +" : "+ e.getMessage() + "\r\n");
    for (StackTraceElement stackTraceElement : stackTrace) {
      stackTraces.append("\t\t" + stackTraceElement.toString() + "\r\n");
    }
    recursivelyCause(e.getCause(), stackTraces);
  }
}
