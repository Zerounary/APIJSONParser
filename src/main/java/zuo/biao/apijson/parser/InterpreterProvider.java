package zuo.biao.apijson.parser;

import java.util.List;

/**
 *  该类延伸出来转换方法，让继承的子类实现
 *  toXXX方法时转换逻辑，需要继承实现
 * @author Zerounary
 *
 */
public abstract class InterpreterProvider<T> extends AbstractProvider{
	
	private T statementTypeHandle;
	private T setsHandle;
	private T selectHandle;
	private T tableHandle;
	private T joinHandle;
	private T innerJoinHandle;
	private T valuesHandle;
	private T outerJoinHandle;
	private T leftOuterJoinHandle;
	private T rightOuterJoinHandle;
	private T whereHandle;
	private T havingHandle;
	private T groupByHandle;
	private T orderByHandle;
	private T columnsHandle;
	private T lastListHandle;
	private T distinctHandle;
	
	public T getStatementTypeHandle() {
		return statementTypeHandle;
	}
	public void setStatementTypeHandle(T statementTypeHandle) {
		setStatementType(toSatementType(statementTypeHandle));
		this.statementTypeHandle = statementTypeHandle;
	}
	public abstract StatementType toSatementType(T charger) ;
	
	public T getSetsHandle() {
		return setsHandle;
	}
	public void setSetsHandle(T setsHandle) {
		setSets(toSets(setsHandle));
		this.setsHandle = setsHandle;
	}
	public abstract List<String> toSets(T charger) ;
	
	public T getSelectHandle() {
		return selectHandle;
	}
	public void setSelectHandle(T selectHandle) {
		setSelect(toSelect(selectHandle));
		this.selectHandle = selectHandle;
	}
	public abstract List<String> toSelect(T charger) ;
	
	public T getTableHandle() {
		return tableHandle;
	}
	public void setTableHandle(T charger) {
		setTables(toTables(charger));
		this.tableHandle = charger;
	}
	public abstract List<String> toTables(T charger) ;
	
	public T getJoinHandle() {
		return joinHandle;
	}
	public void setJoinHandle(T charger) {
		setJoin(toJoin(charger));
		this.joinHandle = charger;
	}
	public abstract List<String> toJoin(T charger) ;
	
	public T getInnerJoinHandle() {
		return innerJoinHandle;
	}
	public void setInnerJoinHandle(T charger) {
		setInnerJoin(toInnerJoin(charger));
		this.innerJoinHandle = charger;
	}
	public abstract List<String> toInnerJoin(T charger) ;
	
	public T getValuesHandle() {
		return valuesHandle;
	}
	public void setValuesHandle(T charger) {
		setValues(toValues(charger));
		this.valuesHandle = charger;
	}
	public abstract List<String> toValues(T charger) ;
	
	public T getOuterJoinHandle() {
		return outerJoinHandle;
	}
	public void setOuterJoinHandle(T charger) {
		setOuterJoin(toOuterJoin(charger));
		this.outerJoinHandle = charger;
	}
	public abstract List<String> toOuterJoin(T charger) ;
	
	public T getLeftOuterJoinHandle() {
		return leftOuterJoinHandle;
	}
	public void setLeftOuterJoinHandle(T charger) {
		setLeftOuterJoin(toLeftOuterJoin(charger));
		this.leftOuterJoinHandle = charger;
	}
	public abstract List<String> toLeftOuterJoin(T charger) ;
	
	public T getRightOuterJoinHandle() {
		return rightOuterJoinHandle;
	}
	public void setRightOuterJoinHandle(T charger) {
		setRightOuterJoin(toRightOuterJoin(charger));
		this.rightOuterJoinHandle = charger;
	}
	public abstract List<String> toRightOuterJoin(T charger) ;
	
	public T getWhereHandle() {
		return whereHandle;
	}
	public void setWhereHandle(T charger) {
		setWhere(toWhere(charger));
		this.whereHandle = charger;
	}
	public abstract List<String> toWhere(T charger) ;
	
	public T getHavingHandle() {
		return havingHandle;
	}
	public void setHavingHandle(T charger) {
		setHaving(toHaving(charger));
		this.havingHandle = charger;
	}
	public abstract List<String> toHaving(T charger) ;
	
	public T getGroupByHandle() {
		return groupByHandle;
	}
	public void setGroupByHandle(T charger) {
		setGroupBy(toGroupBy(charger));
		this.groupByHandle = charger;
	}
	public abstract List<String> toGroupBy(T charger) ;
	
	public T getOrderByHandle() {
		return orderByHandle;
	}
	public void setOrderByHandle(T charger) {
		
		this.orderByHandle = charger;
	}
	public abstract List<String> toOrderBy(T charger) ;
	
	public T getColumnsHandle() {
		return columnsHandle;
	}
	public void setColumnsHandle(T charger) {
		setColumns(toColumns(charger));
		this.columnsHandle = charger;
	}
	public abstract List<String> toColumns(T charger) ;
	
	public T getLastListHandle() {
		return lastListHandle;
	}
	public void setLastListHandle(T charger) {
		setLastList(toLastList(charger));
		this.lastListHandle = charger;
	}
	public abstract List<String> toLastList(T charger) ;
	
	public T getDistinctHandle() {
		return distinctHandle;
	}
	public void setDistinctHandle(T charger) {
		super.setDistinct(toDistinct(charger));
		this.distinctHandle = charger;
	}
	public abstract boolean toDistinct(T charger) ;
	

}
