package nc.impl.rm.demand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import nc.bs.dao.BaseDAO;
import nc.hr.frame.persistence.SimpleDocServiceTemplate;
import nc.hr.utils.FromWhereSQLUtils;
import nc.hr.utils.InSQLCreator;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.hr.utils.StringPiecer;
import nc.itf.rm.IDemandQueryService;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.ui.querytemplate.querytree.FromWhereSQL;
import nc.vo.om.aos.AOSSQLHelper;
import nc.vo.org.DeptVO;
import nc.vo.pub.BusinessException;
import nc.vo.rm.demand.AggRMDemandVO;
import nc.vo.rm.demand.DemandVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class DemandQuerySerivceImpl
  implements IDemandQueryService
{
  private SimpleDocServiceTemplate serviceTemplate;
  
  public DemandQuerySerivceImpl() {}
  
  public AggRMDemandVO[] queryByCondition(String condition) throws Exception
  {
    return (AggRMDemandVO[])getServiceTemplate().queryByCondition(AggRMDemandVO.class, condition);
  }
  

  public DemandVO[] queryByCondition(String pk_dept, boolean isContainSub, String name, String code)
    throws BusinessException
  {
    String[] pk_depts = null;
    if (isContainSub) {
      String sql = AOSSQLHelper.getAllDeptInSQLByHROrgPK(pk_dept, true);
      Collection<DeptVO> result = (Collection)new BaseDAO().executeQuery(sql, new BeanListProcessor(DeptVO.class));
      DeptVO[] deptVOs = CollectionUtils.isEmpty(result) ? null : (DeptVO[])result.toArray(new DeptVO[0]);
      if (ArrayUtils.isEmpty(deptVOs))
        return null;
      pk_depts = StringPiecer.getStrArray(deptVOs, "pk_dept");
    } else {
      pk_depts = new String[1];
      pk_depts[0] = pk_dept;
    }
    InSQLCreator sqlCreator = new InSQLCreator();
    try {
      String inSQL = sqlCreator.getInSQL(pk_depts);
      String sql = " 1=1 ";
      if (!StringUtils.isEmpty(pk_dept)) {
        sql = sql + " and pk_rmdept in (" + inSQL + ") ";
      }
      if (!StringUtils.isEmpty(name)) {
        sql = sql + " and name = '" + name + "'";
      }
      if (!StringUtils.isEmpty(code)) {
        sql = sql + " and code ='" + code + "'";
      }
      Collection<DemandVO> result = new BaseDAO().retrieveByClause(DemandVO.class, sql);
      return CollectionUtils.isEmpty(result) ? null : (DemandVO[])result.toArray(new DemandVO[0]);
    } finally {
      sqlCreator.clear();
    }
  }
  

  public DemandVO[] queryByCondition(String pk_dept, boolean isContainSub, String name, String code, String state)
    throws BusinessException
  {
    String[] pk_depts = null;
    if (isContainSub) {
      String sql = AOSSQLHelper.getAllDeptInSQLByHROrgPK(pk_dept, true);
      Collection<DeptVO> result = (Collection)new BaseDAO().executeQuery(sql, new BeanListProcessor(DeptVO.class));
      DeptVO[] deptVOs = CollectionUtils.isEmpty(result) ? null : (DeptVO[])result.toArray(new DeptVO[0]);
      if (ArrayUtils.isEmpty(deptVOs))
        return null;
      pk_depts = StringPiecer.getStrArray(deptVOs, "pk_dept");
    }
    else {
      pk_depts = new String[1];
      pk_depts[0] = pk_dept;
    }
    InSQLCreator sqlCreator = new InSQLCreator();
    try {
      String inSQL = sqlCreator.getInSQL(pk_depts);
      String sql = " 1=1 ";
      if (StringUtils.isEmpty(pk_dept))
        return null;
      sql = sql + " and pk_rmdept in (" + inSQL + ") ";
      if (!StringUtils.isEmpty(name)) {
        sql = sql + " and name like '%" + name + "%' ";
      }
      if (!StringUtils.isEmpty(code))
        sql = sql + " and code like '%" + code + "%'";
      if (!StringUtils.isEmpty(state)) {
        sql = sql + " and demandstate =" + state;
      }
      Object result = new BaseDAO().retrieveByClause(DemandVO.class, sql);
      return CollectionUtils.isEmpty((Collection)result) ? null : (DemandVO[])((Collection)result).toArray(new DemandVO[0]);
    } finally {
      sqlCreator.clear();
    }
  }
  
  public AggRMDemandVO queryByPk(String pk)
    throws Exception
  {
    return (AggRMDemandVO)getServiceTemplate().queryByPk(AggRMDemandVO.class, pk);
  }
  
  private SimpleDocServiceTemplate getServiceTemplate() {
    if (this.serviceTemplate == null) {
      this.serviceTemplate = new SimpleDocServiceTemplate("33be1477-98f9-4e8d-90bf-4ec7289ed563");
    }
    return this.serviceTemplate;
  }
  

  public DemandVO[] queryByCondition(String pk_dept, String pk_user, boolean isContainSub, FromWhereSQL fromWhereSQL)
    throws BusinessException
  {
    String[] pk_depts = null;
    if (isContainSub) {
      String sql = AOSSQLHelper.getAllDeptInSQLByHROrgPK(pk_dept, true);
      Collection<DeptVO> result = (Collection)new BaseDAO().executeQuery(sql, new BeanListProcessor(DeptVO.class));
      DeptVO[] deptVOs = CollectionUtils.isEmpty(result) ? null : (DeptVO[])result.toArray(new DeptVO[0]);
      if (ArrayUtils.isEmpty(deptVOs))
        return null;
      pk_depts = StringPiecer.getStrArray(deptVOs, "pk_dept");
    }
    else {
      pk_depts = new String[1];
      pk_depts[0] = pk_dept;
    }
    InSQLCreator sqlCreator = new InSQLCreator();
    try {
      String inSQL = sqlCreator.getInSQL(pk_depts);
      String sql = " 1=1 ";
      if (StringUtils.isEmpty(pk_dept))
        return null;
      sql = sql + " and pk_rmdept in (" + inSQL + ") ";
      if ((fromWhereSQL != null) && (fromWhereSQL.getWhere() != null)) {
        String normalsql = FromWhereSQLUtils.createSelectSQL(fromWhereSQL, DemandVO.getDefaultTableName(), new String[] { "pk_demand" }, null, null, null, null);
        normalsql = "pk_demand in (" + normalsql + ")";
        sql = sql + " and " + normalsql;
      }
      if (!StringUtils.isEmpty(pk_user))
        sql = sql + " and " + "billmaker" + " in('" + PubEnv.getPk_user() + "','" + "NC_USER0000000000000" + "') ";
      Object result = new BaseDAO().retrieveByClause(DemandVO.class, sql);
      return CollectionUtils.isEmpty((Collection)result) ? null : (DemandVO[])((Collection)result).toArray(new DemandVO[0]);
    } finally {
      sqlCreator.clear();
    }
  }
  /**
   *��ѯ��λ������Ա����      ������
   */
    nc.util.rm.JDBCUtils JDBCUtils = new nc.util.rm.JDBCUtils();//�������ݿ⹤����
	Connection con = null;// ����һ�����ݿ�����
    PreparedStatement pre = null;// ����Ԥ����������
    ResultSet result = null;// ����һ�����������
	@Override
	public String getBudget(String sql) throws Exception{
		    String str = null;
	        try {
	        	con = JDBCUtils.getConnection();// ��ȡ����         
				pre = con.prepareStatement(sql);//Ԥ����sql			
				result = pre.executeQuery();// ִ�в�ѯ
				while (result.next()){
					str=result.getString(1);					
					return str;
				}
	        }catch(SQLException e){
	        	str="��ѯ�����쳣";
	        	throw new Exception("��ѯ�����쳣");					
			}finally
		    {
				try
		        {
		            // ��һ������ļ�������رգ���Ϊ���رյĻ���Ӱ�����ܡ�����ռ����Դ
		            // ע��رյ�˳�����ʹ�õ����ȹر�
		            if (result != null)
		                result.close();
		            if (pre != null)
		                pre.close();
		            if (con != null)
		                con.close();
		        }		        
		        catch (Exception e)
		        {
		        	throw new Exception("��ѯ�����쳣");
		        }
		    }        	              	     
		    return str;		
	}
}
