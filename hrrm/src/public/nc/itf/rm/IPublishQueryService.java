package nc.itf.rm;

import java.util.Map;
import nc.ui.querytemplate.querytree.FromWhereSQL;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.rm.job.AggRMJobVO;
import nc.vo.rm.publish.AggPublishVO;
import nc.vo.rm.publish.PublishJobVO;

public abstract interface IPublishQueryService
{
  public abstract AggPublishVO queryByPk(String paramString)
    throws BusinessException;
  
  public abstract Map<String, AggRMJobVO> queryByPk(String[] paramArrayOfString)
    throws BusinessException;
  
  public abstract AggRMJobVO queryJobByPk(String paramString)
    throws BusinessException;
  
  public abstract AggPublishVO[] queryPublishJobByActive(String paramString1, String paramString2, String paramString3, UFLiteralDate paramUFLiteralDate, String paramString4,Integer paramInteger)
    throws BusinessException;
  
  public abstract AggPublishVO[] queryByPlace(FromWhereSQL paramFromWhereSQL, Integer paramInteger)
    throws BusinessException;
  
  public abstract PublishJobVO[] queryByWeb(String paramString1, String paramString2, String[] paramArrayOfString)
    throws BusinessException;
  
  public abstract PublishJobVO[] queryByEndDate(String paramString, UFLiteralDate paramUFLiteralDate)
    throws BusinessException;
  
  public abstract AggPublishVO[] queryByCondition(String paramString)
    throws BusinessException;
}
