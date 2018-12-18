package zuo.biao.apijson.parser;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractProvider implements SQLProvider {
	private StatementType statementType;
	private List<String> sets = new ArrayList<String>();
	private List<String> select = new ArrayList<String>();
	private List<String> tables = new ArrayList<String>();
	private List<String> join = new ArrayList<String>();
	private List<String> innerJoin = new ArrayList<String>();
	private List<String> outerJoin = new ArrayList<String>();
	private List<String> leftOuterJoin = new ArrayList<String>();
	private List<String> rightOuterJoin = new ArrayList<String>();
	private List<String> where = new ArrayList<String>();
	private List<String> having = new ArrayList<String>();
	private List<String> groupBy = new ArrayList<String>();
	private List<String> orderBy = new ArrayList<String>();
	private List<String> lastList = new ArrayList<String>();
	private List<String> columns = new ArrayList<String>();
	private List<String> values = new ArrayList<String>();
	private boolean distinct;
	 
	private Message message = new Message();
	
	public Message getMessage() {
		return message;
	};
	/**
	 *  接收错误消息
	 * @param err
	 */
	public void error(String err) {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		//this.message.error(ste.getFileName() + ": Line " + ste.getLineNumber() + " Error message:" +err, ste);
		this.message.error(err, ste);
	}
	
	@Override
	public StatementType getStatementType() {
		// TODO Auto-generated method stub
		return statementType;
	}
	public void setStatementType(StatementType statementType) {
		// TODO Auto-generated method stub
		this.statementType = statementType;
	}

	@Override
	public List<String> getSets() {
		// TODO Auto-generated method stub
		return sets;
	}

	@Override
	public List<String> getSelect() {
		// TODO Auto-generated method stub
		return select;
	}

	@Override
	public List<String> getTables() {
		// TODO Auto-generated method stub
		return tables;
	}

	@Override
	public List<String> getJoin() {
		// TODO Auto-generated method stub
		return join;
	}

	@Override
	public List<String> getInnerJoin() {
		// TODO Auto-generated method stub
		return innerJoin;
	}

	@Override
	public List<String> getOuterJoin() {
		// TODO Auto-generated method stub
		return outerJoin;
	}

	@Override
	public List<String> getLeftOuterJoin() {
		// TODO Auto-generated method stub
		return leftOuterJoin;
	}

	@Override
	public List<String> getRightOuterJoin() {
		// TODO Auto-generated method stub
		return rightOuterJoin;
	}

	@Override
	public List<String> getWhere() {
		// TODO Auto-generated method stub
		return where;
	}

	@Override
	public List<String> getHaving() {
		// TODO Auto-generated method stub
		return having;
	}

	@Override
	public List<String> getGroupBy() {
		// TODO Auto-generated method stub
		return groupBy;
	}

	@Override
	public List<String> getOrderBy() {
		// TODO Auto-generated method stub
		return orderBy;
	}

	@Override
	public List<String> getLastList() {
		// TODO Auto-generated method stub
		return lastList;
	}

	@Override
	public List<String> getColumns() {
		// TODO Auto-generated method stub
		return columns;
	}

	@Override
	public List<String> getValues() {
		// TODO Auto-generated method stub
		return values;
	}

	@Override
	public boolean isDistinct() {
		// TODO Auto-generated method stub
		return distinct;
	}
	public void setSets(List<String> sets) {
		this.sets = sets;
	}
	public void setSelect(List<String> select) {
		this.select = select;
	}
	public void setTables(List<String> tables) {
		this.tables = tables;
	}
	public void setJoin(List<String> join) {
		this.join = join;
	}
	public void setInnerJoin(List<String> innerJoin) {
		this.innerJoin = innerJoin;
	}
	public void setOuterJoin(List<String> outerJoin) {
		this.outerJoin = outerJoin;
	}
	public void setLeftOuterJoin(List<String> leftOuterJoin) {
		this.leftOuterJoin = leftOuterJoin;
	}
	public void setRightOuterJoin(List<String> rightOuterJoin) {
		this.rightOuterJoin = rightOuterJoin;
	}
	public void setWhere(List<String> where) {
		this.where = where;
	}
	public void setHaving(List<String> having) {
		this.having = having;
	}
	public void setGroupBy(List<String> groupBy) {
		this.groupBy = groupBy;
	}
	public void setOrderBy(List<String> orderBy) {
		this.orderBy = orderBy;
	}
	public void setLastList(List<String> lastList) {
		this.lastList = lastList;
	}
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
	public void setValues(List<String> values) {
		this.values = values;
	}
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	
}
