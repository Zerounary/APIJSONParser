package zuo.biao.apijson.parser;

import java.util.List;
/**
 * 实现这个接口提供SQLBuilder获取构造SQL语句所需要的数据
 * @author Zerounary
 *
 */
public interface SQLProvider {
	/**
	 * 消息类，如果Provider中有异常，通过这个方法可以传递消息
	 * @return
	 */
	public Message getMessage();
	/**
	 * StatementType表示要生成的是SELECT,INSERT,UPDATE,DLEETE中的哪种
	 * @return StatementType
	 */
	public StatementType getStatementType();
	/**
	 * StatementType为UPDATE时
	 * UPDATE要更新的字段
	 * @return List<String>
	 */
	public List<String> getSets() ;
	/**
	 * StatementType为SELECT时
	 * SELECT要查询的字段
	 * @return List<String>
	 */
	public List<String> getSelect() ;
	/**
	 * StatementType所有类型都将使用这个方法
	 * SQL所涉及的表
	 * @return
	 */
	public List<String> getTables();
	public List<String> getJoin();
	public List<String> getInnerJoin() ;
	public List<String> getOuterJoin() ;
	public List<String> getLeftOuterJoin() ;
	public List<String> getRightOuterJoin() ;
	/**
	 * SQL过滤条件
	 * @return
	 */
	public List<String> getWhere();
	public List<String> getHaving();
	/**
	 * StatementType为SELECT时
	 * SQL查询时的分组
	 * @return
	 */
	public List<String> getGroupBy() ;
	/**
	 * StatementType为SELECT时
	 * SQL查询时的排序
	 * @return
	 */
	public List<String> getOrderBy() ;
	public List<String> getLastList() ;
	/**
	 * StatementType为INSERT时
	 * SQL新增的字段
	 * @return
	 */
	public List<String> getColumns() ;
	/**
	 * StatementType为INSERT时
	 * SQL新增字段所对应的值
	 * @return
	 */
	public List<String> getValues() ;
	public boolean isDistinct() ;
	
	
}
