package zuo.biao.apijson.parser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class APIJSONProvider extends AbstractProvider {
	
	private final String ALIAS_SPLIT = ":";
	private static final String AND = " AND ";
	private static final String OR = " OR ";

	private List<String> tableWhiteList = new ArrayList<String>();
	private List<String> tableBlackList = new ArrayList<String>();
	private List<String> columnWhiteList = new ArrayList<String>();
	private List<String> columnBlackList = new ArrayList<String>();
	
	private JSONObject request;
	private JSONObject join;
	
	/*
	 * ==================================
	 *              通用逻辑
	 * ==================================
	 * 
	 * 所有操作都会涉及到的内容
	 * 
	 */
	
	/**
	 * 传入的参数应该是一个通过验证的APIJSON请求
	 * @param request
	 */
	public APIJSONProvider(JSONObject obj) {
		if(obj == null) {
			error("APIJSONProvider传入的请求不能为空");
		}
		JSONObject tabs = obj.getJSONObject("[]");
		if(tabs == null) {
			this.request = obj;
		}else {
			this.request = tabs;
			this.join = obj.getJSONObject("join");
		}
	}
	/**
	 * 解析请求中的表名
	 * 表名必须符合：(\w+(:\w+)?)
	 * 即：
	 *    表名
	 *    表名:表别名
	 * 两种形式
	 */
	@Override
	public List<String> getTables() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		Set<String> tableNames = request.keySet();
		String tableAliasName;
		String tableRealName;
		for(String tableName : tableNames) {
			if(!tableName.matches("(\\w+(:\\w+)?)")) {
				error("表" + tableName + "格式不符合" );
				return null;
			}
			//是否有自定义别名
			if(tableName.contains(ALIAS_SPLIT)) {
				String[] splitArray = tableName.split(ALIAS_SPLIT);
				tableRealName = splitArray[0];
				tableAliasName = splitArray[1];
			}else {
				tableRealName = tableAliasName = tableName;
			}
			validateTable(tableRealName);
			list.add(tableRealName + " " + tableAliasName);
		}
		return list;
	}
	
	/*
	 * ==================================
	 *              查询逻辑
	 * ==================================
	 */
	
	/**
	 * 解析请求中的字段
	 * 路径：/表名/@column
	 * 同时为字段设置好引用的表别名，如: p.id
	 * 表名必须符合：(\w+(:\w+)?)
	 * 字段值必须符合：
	 * 不支持函数的正则：(\w+(:\w+)?)+(\s?,\s?(\w+(:\w+)?)+)*     
	 * 支持函数的正则：((\w+\(\w+\):\w+|\w+)(:\w+)?)+(\s?,\s?((\w+\(\w+\):\w+|\w+)(:\w+)?)+)*
	 * 例：a,b,c或a:a1,b:b1,c
	 * 约束：必须要有表名
	 * 目前支持函数
	 */
	@Override
	public List<String> getSelect() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.SELECT) {
			Set<String> tableNames = request.keySet();
			String tableRealName;
			String tableAliasName;
			for(String tableName : tableNames) {
				//格式检查
				if(!tableName.matches("(\\w+(:\\w+)?)")) {
					error("表" + tableName + "格式不符合" );
					return null;
				}
				//是否有自定义别名
				if(tableName.contains(ALIAS_SPLIT)) {
					tableRealName = tableName.split(ALIAS_SPLIT)[0];
					//填写了表别名
					tableAliasName = tableName.split(ALIAS_SPLIT)[1];
				}else {
					tableRealName = tableAliasName = tableName;
				}
				// 获取请求@column的值
				JSONObject propertis = request.getJSONObject(tableName);
				String columnsValue = propertis.getString("@column");
				
				if(columnsValue == null) {
					validateColumn(tableRealName,"*");
					//没有填写@column字段，默认为全部
					list.add(tableAliasName + ".*");
				}else {
					if(!columnsValue.matches("((\\w+\\(\\w+\\):\\w+|\\w+)(:\\w+)?)+(\\s?,\\s?((\\w+\\(\\w+\\):\\w+|\\w+)(:\\w+)?)+)*")) {
						error("字段@column：" + columnsValue + "格式不符合，正确请求如：a,b,c:d或者a,max(d):d,c:e" );
						return null;
					}
					//填写了，则返回tableAliasName.colName或tableAliasName.columnName as columnAliasName
					String[] columnNames = columnsValue.replaceAll("\\s", "").split(",");
					for(String columnName : columnNames) {
						if(columnName.contains(ALIAS_SPLIT)) {
							//填写了字段别名，使用：tableAliasName.columnName as columnAliasName这种类型
							//这里columnRealName有两种情况，如：id或max(id)
							String functionOrColumn = columnName.split(ALIAS_SPLIT)[0];
							if(functionOrColumn.contains("(")) {
								//有函数的字段
								//去掉)
								functionOrColumn = functionOrColumn.replaceAll("\\)", "");
								String[] functionStrs = functionOrColumn.split("\\(");
								String funcitonName = functionStrs[0];
								//要对函数进行控制这这里进行
								//此处省略函数合法性检查的代码...
								String columnRealName =  functionStrs[1];
								String columnAliasName = columnName.split(ALIAS_SPLIT)[1];
								validateColumn(tableRealName,columnRealName);
								list.add(funcitonName + "(" + tableAliasName + "." + columnRealName + ")" + " as " + columnAliasName);
							}else {
								//没函数的字段
								String columnRealName = functionOrColumn ;
								String columnAliasName = columnName.split(ALIAS_SPLIT)[1];
								validateColumn(tableRealName,columnRealName);
								list.add(tableAliasName + "." + columnRealName + " as " + columnAliasName);
							}
						}else {
							//使用tableAliasName.columnName类型
							validateColumn(tableRealName,columnName);
							list.add(tableAliasName + "." + columnName);
						}
					}
				}
				
			}
		}
		return list;
	}
	/**
	 * 解析请求中的过滤条件
	 * 支持以下模式：
	 *   单值："id" : "12" 即id=12
	 *   多值："id&{}" : ">12,<30" 即 id > 12 AND id < 30
	 *   多值："id|{}" : ">12,<30" 即 id > 12 OR id < 30
	 *   多值："id{}" : [1,2,3] 即 id IN (1,2,3)
	 *   多值："id!{}" : [1,2,3] 即 id NOT IN (1,2,3)
	 *   模糊："content~":"keyword" 即字段content包含字符串keyword
	 *   模糊："content~":"%keyword" 同样是模糊查询，%放出来自己操控
	 *   正则："content?":"^[0-9]+$" 后面填写正则即可
	 *   外键："id@":"/外键表/外键字段"
	 */
	@Override
	public List<String> getWhere() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		Set<String> tableNames = request.keySet();
		String tableAliasName;
		for(String tableName : tableNames) {
			//是否有自定义别名
			if(tableName.contains(ALIAS_SPLIT)) {
				//填写了表别名
				tableAliasName = tableName.split(ALIAS_SPLIT)[1];
			}else {
				tableAliasName = tableName;
			}
			// 遍历过滤条件
			JSONObject propertis = request.getJSONObject(tableName);
			conditionLoop:for(String condition : propertis.keySet() ) {
				//关键字则跳过
				if(condition.startsWith("@")) {
					continue conditionLoop;
				}
					if(condition.matches("\\w+")) {	
						//纯字段名
						if(propertis.get(condition) instanceof Integer || 
						   propertis.get(condition) instanceof Float   ||
						   propertis.get(condition) instanceof Double  ||
						   propertis.get(condition) instanceof BigDecimal) {
							list.add(tableAliasName + "." + condition + " = " + propertis.get(condition) );
						}else if(propertis.get(condition) instanceof String) {
							list.add(tableAliasName + "." + condition + " = '" + ((String) propertis.get(condition)).replaceAll("'", "''") + "'" );
						}
					}else if(condition.matches("(\\w+(!|&|\\|)?\\{\\})+")) {
						
						//表示这是一个多条件类型
						if(propertis.get(condition) instanceof String) {
							String exp = (String)propertis.get(condition);
							//是否匹配这种类型：<20.3,>3.3,=3.3
							if(!exp.matches("(\\s?(>|<|>=|<=|=|<>)+\\s?((\\-|\\+)?\\d+(\\.\\d+)?)+)+(\\s?,\\s?((>|<|>=|<=|=|<>)+\\s?((\\-|\\+)?\\d+(\\.\\d+)?)))*")) {
								error(condition + "的格式不正确，正确使用方式如: >10,<20");
								return null;
							}
							if(condition.endsWith("|{}")) {
								String[] terms = exp.replaceAll("\\s", "").split(",");
								String columnName = condition.replaceAll("\\|\\{\\}", "");
								String limit = "";
								for(int i = 0; i < terms.length; i++) {
									String term = terms[i];
									if(term.startsWith(">=")) {
										limit += tableAliasName + "." + columnName + " >= " + term.replaceAll(">=", "");
									}else if(term.startsWith("<=")) {
										limit += tableAliasName + "." + columnName + " <= " + term.replaceAll("<=", "");
									}else if(term.startsWith(">")) {
										limit += tableAliasName + "." + columnName + " > " + term.replaceAll(">", "");
									}else if(term.startsWith("<")) {
										limit += tableAliasName + "." + columnName + " < " + term.replaceAll("<", "");
									}else if(term.startsWith("<>") || term.startsWith("!=")  ) {
										limit += tableAliasName + "." + columnName + " <> " + term.replaceAll("<>", "").replaceAll("!=", "");
									}else if(term.startsWith("=")) {
										limit += tableAliasName + "." + columnName + " = " + term.replaceAll("=", "");
									}
									if(terms.length > 1 && (i != (terms.length - 1)))
										limit += OR;
								}
								list.add(limit);
							}else {
								getLastList().add(AND);
								String[] terms = exp.replaceAll("\\s", "").split(",");
								String columnName = condition.replaceAll("\\{\\}", "").replaceAll("&", "").replaceAll("!", "");
								String limit = "";
								for(int i = 0; i < terms.length; i++) {
									String term = terms[i];
									if(term.startsWith(">=")) {
										limit += tableAliasName + "." + columnName + " >= " + term.replaceAll(">=", "");
									}else if(term.startsWith("<=")) {
										limit += tableAliasName + "." + columnName + " <= " + term.replaceAll("<=", "");
									}else if(term.startsWith(">")) {
										limit += tableAliasName + "." + columnName + " > " + term.replaceAll(">", "");
									}else if(term.startsWith("<")) {
										limit += tableAliasName + "." + columnName + " < " + term.replaceAll("<", "");
									}else if(term.startsWith("<>") || term.startsWith("!=")  ) {
										limit += tableAliasName + "." + columnName + " <> " + term.replaceAll("<>", "").replaceAll("!=", "");
									}else if(term.startsWith("=")) {
										limit += tableAliasName + "." + columnName + " = " + term.replaceAll("=", "");
									}
									if(terms.length > 1 && (i != (terms.length - 1)))
										limit += AND;
								}
								list.add(limit);
								
							}
						}else if(propertis.get(condition) instanceof JSONArray) {
							JSONArray array = (JSONArray)propertis.get(condition);
							//分离出字段名
							String columnName = condition.replaceAll("\\{\\}", "").replaceAll("&", "").replaceAll("!", "");
							String limit = "";
							if(condition.endsWith("!{}")) {
								if(!array.isEmpty()) {
									for(int i = 0; i < array.size(); i++) {
										Object obj = array.get(i);
										if(i != 0) {
											limit += ",";
										}
										if( obj instanceof Integer || 
											obj instanceof Float   ||
											obj instanceof Double  ||
											obj instanceof BigDecimal) {
											limit += obj;
										}else if(obj instanceof String) {
											limit += "'" + ((String)obj).replaceAll("'", "''") + "'";
										}
									}
									list.add(tableAliasName + "." + columnName + " NOT IN [" + limit + "]" );
								}
							}else {
								if(!array.isEmpty()) {
									for(int i = 0; i < array.size(); i++) {
										Object obj = array.get(i);
										if(i != 0) {
											limit += " , ";
										}
										if( obj instanceof Integer || 
											obj instanceof Float   ||
											obj instanceof Double  ||
											obj instanceof BigDecimal) {
											limit += obj;
										}else if(obj instanceof String) {
											limit += "'" + ((String)obj).replaceAll("'", "''") + "'";
										}
									}
									list.add(tableAliasName + "." + columnName + " IN [" + limit + "]" );
								}
							}
						}
					}else if(condition.matches("\\w+~")) {
						//字符串查询，包含
						if(propertis.get(condition) instanceof String) {
							String exp = (String)propertis.get(condition);
							String columnName = condition.replaceAll("~", "");
							list.add(tableAliasName + "." + columnName + " LIKE '%" + exp.replaceAll("'", "''") + "%'" );
						}else {
							error(condition +"的值必须要是字符串");
							return null;
						}
					}else if(condition.matches("\\w+$")) {
						//字符串查询，LIKE
						if(propertis.get(condition) instanceof String) {
							String exp = (String)propertis.get(condition);
							String columnName = condition.replaceAll("$", "");
							list.add(tableAliasName + "." + columnName + " LIKE '" + exp.replaceAll("'", "''") + "'" );
						}else {
							error(condition +"的值必须要是字符串");
							return null;
						}
					}else if(condition.matches("\\w+\\?")) {
						//字符串查询，正则
						if(propertis.get(condition) instanceof String) {
							String exp = (String)propertis.get(condition);
							String columnName = condition.replaceAll("\\?", "");
							list.add(" regexp_like(" + tableAliasName + "." + columnName + ",'" + exp.replaceAll("'", "''") + "')" );
						}else {
							error(condition +"的值必须要是字符串");
							return null;
						}
					}else if(condition.matches("\\w+@")) {
						//内连接
						if(propertis.get(condition) instanceof String) {
							String exp = (String)propertis.get(condition);
							String columnName = condition.replaceAll("@", "");
							if(exp.matches("/\\w+/\\w+")) {
								String[] args = exp.split("/");
								String refTable = args[1];
								String refColumn = args[2];
								list.add(refTable + "." + refColumn + " = " + tableAliasName + "." + columnName);
							}else {
								error(condition + "必须符合：\"/表名或别名/字段名\"的形式");
								return null;
							}
						}else {
							error(condition +"的值必须要是字符串");
							return null;
						}
					}
				}
			
			
		}
		return list;
	}
	/**
	 * 内连接
	 * 请求："@innerJoin" : ["table1.column1 = table2.column2","table1.column1 = table2.column2"]
	 * 编译之后：INNER JOIN table1 ON table1.column1=table2.column2
	 */
	@Override
	public List<String> getInnerJoin() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.SELECT) {
			if(join != null && join.get("@innerJoin")!=null) {
				JSONArray stms = join.getJSONArray("@innerJoin");
				for(int i = 0; i < stms.size(); i++) {
					Object obj = stms.get(i);
					if(obj instanceof String) {
						String joinStr = (String)obj;
						if(joinStr.matches("\\w+\\.\\w+\\s?=\\s?\\w+\\.\\w+")) {
							//table1.column1 = table2.column2
							String[] tcs = joinStr.replaceAll("\\s", "").split("=");
							String leftTable = tcs[0].split("\\.")[0];
							String rightTable = tcs[1].split("\\.")[0];
							validateTable(leftTable);
							list.add(leftTable + " ON " + joinStr);
						}else {
							error("@innerJoin的格式必须是：table1.column1 = table2.column2,相当于INNER JOIN table1 ON table1.column1=table2.column2");
							return null;
						}
					}else {
						error("@innerJoin的类型必须是String类型，填写的值如：table1.column1 = table2.column2,相当于INNER JOIN table1 ON table1.column1=table2.column2。注意：表有别名的应该写表别名");
						return null;
					}
				}
			}
		}
		return list;
	}
	/**
	 * 左外连接
	 * 
	 */
	@Override
	public List<String> getLeftOuterJoin() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.SELECT) {
			if(join != null && join.get("@leftOuterJoin")!=null) {
				JSONArray stms = join.getJSONArray("@leftOuterJoin");
				for(int i = 0; i < stms.size(); i++) {
					Object obj = stms.get(i);
					if(obj instanceof String) {
						String joinStr = (String)obj;
						if(joinStr.matches("\\w+\\.\\w+\\s?=\\s?\\w+\\.\\w+")) {
							//table1.column1 = table2.column2
							String[] tcs = joinStr.replaceAll("\\s", "").split("=");
							String leftTable = tcs[0].split("\\.")[0];
							String rightTable = tcs[1].split("\\.")[0];
							validateTable(leftTable);
							list.add(leftTable + " ON " + joinStr);
						}else {
							error("@leftOuterJoin的格式必须是：table1.column1 = table2.column2,相当于LEFT OUTER JOIN table1 ON table1.column1=table2.column2");
							return null;
						}
					}else {
						error("@leftOuterJoin的类型必须是String类型，填写的值如：table1.column1 = table2.column2,相当于LEFT OUTER JOIN table1 ON table1.column1=table2.column2。注意：表有别名的应该写表别名");
						return null;
					}
				}
			}
		}
		return list;
	}
	/**
	 * 右外链接
	 */
	@Override
	public List<String> getRightOuterJoin() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.SELECT) {
			if(join != null && join.get("@rightOuterJoin")!=null) {
				JSONArray stms = join.getJSONArray("@rightOuterJoin");
				for(int i = 0; i < stms.size(); i++) {
					Object obj = stms.get(i);
					if(obj instanceof String) {
						String joinStr = (String)obj;
						if(joinStr.matches("\\w+\\.\\w+\\s?=\\s?\\w+\\.\\w+")) {
							//table1.column1 = table2.column2
							String[] tcs = joinStr.replaceAll("\\s", "").split("=");
							String leftTable = tcs[0].split("\\.")[0];
							String rightTable = tcs[1].split("\\.")[0];
							validateTable(leftTable);
							list.add(leftTable + " ON " + joinStr);
						}else {
							error("@rightOuterJoin的格式必须是：table1.column1 = table2.column2,相当于RIGHT OUTER JOIN table1 ON table1.column1=table2.column2");
							return null;
						}
					}else {
						error("@rightOuterJoin的类型必须是String类型，填写的值如：table1.column1 = table2.column2,相当于RIGHT OUTER JOIN table1 ON table1.column1=table2.column2。注意：表有别名的应该写表别名");
						return null;
					}
				}
			}
		}
		return list;
	}
	/**
	 * join连接
	 */
	@Override
	public List<String> getJoin() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.SELECT) {
			if(join != null && join.get("@join")!=null) {
				JSONArray stms = join.getJSONArray("@join");
				for(int i = 0; i < stms.size(); i++) {
					Object obj = stms.get(i);
					if(obj instanceof String) {
						String joinStr = (String)obj;
						if(joinStr.matches("\\w+\\.\\w+\\s?=\\s?\\w+\\.\\w+")) {
							//table1.column1 = table2.column2
							String[] tcs = joinStr.replaceAll("\\s", "").split("=");
							String leftTable = tcs[0].split("\\.")[0];
							String rightTable = tcs[1].split("\\.")[0];
							validateTable(leftTable);
							list.add(leftTable + " ON " + joinStr);
						}else {
							error("@join的格式必须是：table1.column1 = table2.column2,相当于JOIN table1 ON table1.column1=table2.column2");
							return null;
						}
					}else {
						error("@join的类型必须是String类型，填写的值如：table1.column1 = table2.column2,相当于JOIN table1 ON table1.column1=table2.column2。注意：表有别名的应该写表别名");
						return null;
					}
				}
			}
		}
		return list;
	}
	/**
	 * 外连接
	 */
	@Override
	public List<String> getOuterJoin() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.SELECT) {
			if(join != null && join.get("@outerJoin")!=null) {
				JSONArray stms = join.getJSONArray("@join");
				for(int i = 0; i < stms.size(); i++) {
					
					Object obj = stms.get(i);
					if(obj instanceof String) {
						String joinStr = (String)obj;
						if(joinStr.matches("\\w+\\.\\w+\\s?=\\s?\\w+\\.\\w+")) {
							//table1.column1 = table2.column2
							String[] tcs = joinStr.replaceAll("\\s", "").split("=");
							String leftTable = tcs[0].split("\\.")[0];
							String rightTable = tcs[1].split("\\.")[0];
							validateTable(leftTable);
							list.add(leftTable + " ON " + joinStr);
						}else {
							error("@outerJoin的格式必须是：table1.column1 = table2.column2,相当于OUTER JOIN table1 ON table1.column1=table2.column2");
							return null;
						}
					}else {
						error("@outerJoin的类型必须是String类型，填写的值如：table1.column1 = table2.column2,相当于OUTER JOIN table1 ON table1.column1=table2.column2。注意：表有别名的应该写表别名");
						return null;
					}
				}
			}
		}
		return list;
	}
	/**
	 * 解析分组
	 * "@group":"store_id"
	 */
	@Override
	public List<String> getGroupBy() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.SELECT) {
			Set<String> tableNames = request.keySet();
			String tableAliasName;
			for(String tableName : tableNames) {
				//格式检查
				if(!tableName.matches("(\\w+(:\\w+)?)")) {
					error("表" + tableName + "格式不符合" );
					return null;
				}
				//是否有自定义别名
				if(tableName.contains(ALIAS_SPLIT)) {
					//填写了表别名
					tableAliasName = tableName.split(ALIAS_SPLIT)[1];
				}else {
					tableAliasName = tableName;
				}
				// 获取请求@group的值
				JSONObject propertis = request.getJSONObject(tableName);
				String columnsValue = propertis.getString("@group");
				if(columnsValue != null) {
					if(!columnsValue.matches("\\w+(\\s?,\\s?\\w+)*")) {
						error("字段@group：" + columnsValue + "格式不符合，正确请求如：a,b,c" );
						return null;
					}
					String[] columnNames = columnsValue.replaceAll("\\s", "").split(",");
					for(String colmunName : columnNames) {
						list.add(tableAliasName + "." + colmunName);
					}
				}
				
			}
		}
		return list;
	}
	
	/**
	 * 解析排序逻辑
	 * column1+,column2-,+表示升序，-表示降序
	 */
	@Override
	public List<String> getOrderBy() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.SELECT) {
			Set<String> tableNames = request.keySet();
			String tableAliasName;
			for(String tableName : tableNames) {
				//格式检查
				if(!tableName.matches("(\\w+(:\\w+)?)")) {
					error("表" + tableName + "格式不符合" );
					return null;
				}
				//是否有自定义别名
				if(tableName.contains(ALIAS_SPLIT)) {
					//填写了表别名
					tableAliasName = tableName.split(ALIAS_SPLIT)[1];
				}else {
					tableAliasName = tableName;
				}
				// 获取请求@column的值
				JSONObject propertis = request.getJSONObject(tableName);
				String columnsValue = propertis.getString("@orders");
				if(columnsValue != null) {
					if(!columnsValue.matches("(\\w+(\\+|\\-)?)+(\\s?,\\s?(\\w+(\\+|\\-)?)+)*")) {
						error("字段@orders：" + columnsValue + "格式不符合，正确格式如：column1+,column2-,+表示升序，-表示降序。" );
						return null;
					}
					//没有填写@orders字段，默认为全部
					String[] columnNames = columnsValue.replaceAll("\\s", "").split(",");
					for(String columnName : columnNames) {
						if(columnName.endsWith("+"))
							list.add(tableAliasName + "." + columnName.replaceAll("\\+", "") + " ASC");
						else if (columnName.endsWith("-"))
							list.add(tableAliasName + "." + columnName.replaceAll("\\-", "") + " DESC");
					}
				}
			}
		}
		return list;
	}
	
	/*
	 * ==================================
	 *              新增逻辑
	 * ==================================
	 */
	
	@Override
	public List<String> getColumns() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.INSERT) {
			Set<String> tableNames = request.keySet();
			if(tableNames.size()!=1) {
				error("新增时，表只能有一个");
				return null;
			}
			for(String tableName : tableNames) {
				//是否有自定义别名
				if(tableName.contains(ALIAS_SPLIT)) {
					//填写了表别名
					error("新增时，表不需要有别名");
					return null;
				}
				// 遍历过滤条件
				JSONObject propertis = request.getJSONObject(tableName);
				for(String condition : propertis.keySet() ) {
					//关键字则跳过
					if(condition.endsWith("@"))
						continue;
					if(condition.matches("\\w+")) {	
						//纯字段名
						validateColumn(tableName,condition);
						list.add(condition);
					}else {
						error("新增时，"+condition+"必须是字段名");
						return null;
					}
				}
				
			}
		}
		return list;
	}
	
	@Override
	public List<String> getValues() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.INSERT) {
			Set<String> tableNames = request.keySet();
			if(tableNames.size()!=1) {
				error("新增时，表只能有一个");
				return null;
			}
			for(String tableName : tableNames) {
				//是否有自定义别名
				if(tableName.contains(ALIAS_SPLIT)) {
					//填写了表别名
					error("新增时，表不需要有别名");
					return null;
				}
				// 遍历过滤条件
				JSONObject propertis = request.getJSONObject(tableName);
				for(String condition : propertis.keySet() ) {
					//关键字则跳过
					if(condition.endsWith("@"))
						continue;
					if(condition.matches("\\w+")) {	
						//纯字段名
						if(propertis.get(condition) instanceof Integer || 
						   propertis.get(condition) instanceof Float   ||
						   propertis.get(condition) instanceof Double  ||
						   propertis.get(condition) instanceof BigDecimal) {
							list.add(propertis.get(condition).toString() );
						}else if(propertis.get(condition) instanceof String) {
							list.add("'" + ((String) propertis.get(condition)).replaceAll("'", "''") + "'");
						}
					}else {
						error("新增时，"+condition+"必须是字段名");
					}
				}
				
			}
		}
		return list;
	}

	/*
	 * ==================================
	 *              修改逻辑
	 * ==================================
	 */
	
	/**
	 * 更新字段
	 * "@description": "20190101,元旦快乐"
	 * 有@标记的才会更新，否则会被认为是WHERE的条件
	 */
	@Override
	public List<String> getSets() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(getStatementType() == StatementType.UPDATE) {
			Set<String> tableNames = request.keySet();
			if(tableNames.size()!=1) {
				error("更新时，表只能有一个");
				return null;
			}
			for(String tableName : tableNames) {
				//是否有自定义别名
				if(tableName.contains(ALIAS_SPLIT)) {
					//填写了表别名
					error("更新时，表不需要有别名");
					return null;
				}
				// 遍历过滤条件
				JSONObject propertis = request.getJSONObject(tableName);
				for(String condition : propertis.keySet() ) {
					//关键字则跳过
					if(condition.matches("@\\w+")) {	
						//要更新的字段
						String columnName = condition.replaceAll("@", "");
						if(propertis.get(condition) instanceof Integer || 
						   propertis.get(condition) instanceof Float   ||
						   propertis.get(condition) instanceof Double  ||
						   propertis.get(condition) instanceof BigDecimal) {
							validateColumn(tableName,columnName);
							list.add(tableName + "." + columnName + "=" + propertis.get(condition).toString() );
						}else if(propertis.get(condition) instanceof String) {
							validateColumn(tableName, columnName);
							list.add(tableName + "." + columnName + "=" + "'" + ((String) propertis.get(condition)).replaceAll("'", "''") + "'");
						}
					}
				}
				
			}
		}
		return list;
	}
	
	/*
	 * ==================================
	 *              权限逻辑
	 * ==================================
	 * 
	 * 不管权限认证系统有多复杂，最后到生成SQL这步
	 * 都是进行黑白名单的检查
	 * 不论黑白，只要名单为空，表示所有数据都可以
	 * 表名单：
	 *   格式：表名
	 *   大小写不敏感
	 * 字段名单：
	 *   格式：表名.字段
	 *   大小写不敏感
	 * 所有字段：
	 *   格式： 表名.*
	 * 
	 * 如果想要新增或者修改的更复杂的逻辑，
	 * 请在外层处理完成之后以制定格式提交该SQL的黑白名单即可
	 *
	 */	
	/**
	 * 表的黑白名单检查
	 * @param tableName
	 */
	private void validateTable(String tableName) {
		String tableUCN = tableName.toUpperCase();
		if(tableBlackList != null && !tableBlackList.isEmpty()) {
			for(String tn : tableBlackList) {
				if(tableUCN.equals(tn.toUpperCase())) {
					error("请求的表:" + tableName + "在黑名单中");
					return;
				}
			}
		}
		
		if(tableWhiteList != null && !tableWhiteList.isEmpty()) {
			for(String tn : tableWhiteList) {
				if(tableUCN.equals(tn.toUpperCase())) {
					return;
				}
			}
			error("请求的表:" + tableName + "不在白名单中");
		}
	}
	/**
	 * 字段黑白名单检查
	 * 要求： "表.列"
	 * @param columnName
	 */
	private void validateColumn(String tableName, String columnName) {
		String tableUCN = tableName.toUpperCase();
		String columnUCN = columnName.toUpperCase();
		String tcUCN = tableUCN + "." + columnUCN;
		if(columnBlackList != null && !columnBlackList.isEmpty()) {
			for(String tc : columnBlackList) {
				if(tcUCN.equals(tc.toUpperCase())) {
					error("请求的字段:" + tcUCN + "在黑名单中");
					return;
				}
			}
		}
		
		if(columnWhiteList != null && !columnWhiteList.isEmpty()) {
			for(String tc : columnWhiteList) {
				if(tc.endsWith(".*") || tcUCN.equals(tc.toUpperCase())) {
					return;
				}
			}
			error("请求的字段:" + tcUCN + "不在白名单中");
		}
	}

	public List<String> getTableWhiteList() {
		return tableWhiteList;
	}
	public void setTableWhiteList(List<String> tableWhiteList) {
		this.tableWhiteList = tableWhiteList;
	}
	public List<String> getTableBlackList() {
		return tableBlackList;
	}
	public void setTableBlackList(List<String> tableBlackList) {
		this.tableBlackList = tableBlackList;
	}
	public List<String> getColumnWhiteList() {
		return columnWhiteList;
	}
	public void setColumnWhiteList(List<String> columnWhiteList) {
		this.columnWhiteList = columnWhiteList;
	}
	public List<String> getColumnBlackList() {
		return columnBlackList;
	}
	public void setColumnBlackList(List<String> columnBlackList) {
		this.columnBlackList = columnBlackList;
	}
}
