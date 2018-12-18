package zuo.biao.apijson.parser;

import java.util.ArrayList;
import java.util.List;

import zuo.biao.apijson.parser.Message.ErrorElement;


public class SQLProviderException extends Exception {
	
	private List<ErrorElement> errors;
	public SQLProviderException() {
		
	}
	public SQLProviderException(String message) {
		super(message);
	}
	public SQLProviderException(Throwable cause) {
		super(cause);
	}
	public SQLProviderException(List<ErrorElement> errors) {
		List<StackTraceElement> stackTrace = new ArrayList<>();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < errors.size(); i++) {
			sb.append( i + ".Error message: ----> " + errors.get(i).getErrorMsg() + Utils.NEW_LINE);
			stackTrace.add(errors.get(i).getStack());
		}
		System.err.print(sb.toString());
		StackTraceElement[] stack = new StackTraceElement[stackTrace.size()];
		super.setStackTrace(stackTrace.toArray(stack));
		this.errors = errors;
	}
}
