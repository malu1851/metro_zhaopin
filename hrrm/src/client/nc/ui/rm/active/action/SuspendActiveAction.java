package nc.ui.rm.active.action;

import java.awt.event.ActionEvent;

import nc.bs.framework.common.NCLocator;
import nc.hr.utils.ResHelper;
import nc.itf.uap.IUAPQueryBS;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.rm.active.model.RMActiveAppModel;
import nc.ui.trade.business.HYPubBO_Client;
import nc.ui.uif2.model.AbstractUIAppModel;
import nc.vo.pub.BusinessException;
import nc.vo.rm.active.ActiveJobVO;
import nc.vo.rm.active.ActiveVO;
import nc.vo.rm.active.AggActiveVO;
import nc.vo.rm.publish.PublishJobVO;
import nc.vo.uif2.LoginContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;



public class SuspendActiveAction
  extends HrAction
{
  private HYPubBO_Client  hc = new HYPubBO_Client();
  public SuspendActiveAction()
  {
    putValue("Code", "SuspendActive");
    setBtnName("ÔÝÍ£»î¶¯");
  }
  
  protected boolean isActionEnable()
  {
    if ((StringUtils.isEmpty(getModel().getContext().getPk_org())) || (getModel().getSelectedData() == null))
      return false;
    AggActiveVO aggvo = (AggActiveVO)getModel().getSelectedData();
    ActiveVO vo = (ActiveVO)aggvo.getParentVO();
    if ((vo.getActivestate() == null) || (vo.getActivestate().intValue() == 1))
      return true;
    return false;
  }
  
  public void doAction(ActionEvent e) throws Exception
  {
    checkDataPermission();
    AggActiveVO aggvo = (AggActiveVO)getModel().getSelectedData();
    ActiveVO vo = (ActiveVO)aggvo.getParentVO();
	//IUAPQueryBS iquery = (IUAPQueryBS) NCLocator.getInstance().lookup(IUAPQueryBS.class.getName());
	String str  = "pk_activity = '"+vo.getPk_active()+"'";
	hc.deleteByWhereClause(PublishJobVO.class,str);
	vo.setActivestate(Integer.valueOf(0));
	aggvo.setParentVO(vo);
	((RMActiveAppModel)getModel()).update(aggvo);
  }
}
