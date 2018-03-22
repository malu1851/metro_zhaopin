package nc.itf.rm;

import nc.ui.querytemplate.querytree.FromWhereSQL;
import nc.vo.pub.BusinessException;
import nc.vo.rm.demand.AggRMDemandVO;
import nc.vo.rm.demand.DemandVO;

public abstract interface IDemandQueryService
{
  public abstract AggRMDemandVO[] queryByCondition(String paramString)
    throws Exception;
  
  public abstract DemandVO[] queryByCondition(String paramString1, boolean paramBoolean, String paramString2, String paramString3)
    throws BusinessException;
  
  public abstract AggRMDemandVO queryByPk(String paramString)
    throws Exception;
  
  public abstract DemandVO[] queryByCondition(String paramString1, boolean paramBoolean, String paramString2, String paramString3, String paramString4)
    throws BusinessException;
  
  public abstract DemandVO[] queryByCondition(String paramString1, String paramString2, boolean paramBoolean, FromWhereSQL paramFromWhereSQL)
    throws BusinessException;
  //查询单位或者部门缺编人数
  public abstract String getBudget(String sql) throws Exception;
}
