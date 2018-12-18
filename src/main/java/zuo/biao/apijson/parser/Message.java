package zuo.biao.apijson.parser;

import java.util.ArrayList;
import java.util.List;
/**
 * 消息通知类，通过这个类型把Provider的消息传递给SQLBuilder
 * 比如：当我们自定义操作遇到异常需要抛出异常的时候，往这个类里面塞StackTraceElement异常
 *  之后传到SQLBuilder中getSQL方法，统一抛出异常。
 * @author Zerounary
 *
 */
public class Message {
	private List<ErrorElement> errors = new ArrayList<>();

	public List<ErrorElement> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorElement> errors) {
		this.errors = errors;
	}
	
	public void error(String errorMsg, StackTraceElement stack) {
		errors.add(new ErrorElement(errorMsg, stack));
	}
	
	public void cleanErrors() {
		errors.clear();
	}
	
	public class ErrorElement {
		private String errorMsg;
		private StackTraceElement stack;
		public ErrorElement() {
			
		}
		public ErrorElement(String errorMsg, StackTraceElement stack) {
			this.errorMsg = errorMsg;
			this.stack = stack;
		}
		public String getErrorMsg() {
			return errorMsg;
		}
		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}
		public StackTraceElement getStack() {
			return stack;
		}
		public void setStack(StackTraceElement stack) {
			this.stack = stack;
		}
		
	}
}

