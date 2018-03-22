package nc.ui.rm.active.action;

import java.awt.event.ActionEvent;
import nc.hr.utils.ResHelper;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.rm.active.model.RMActiveAppModel;
import nc.ui.uif2.model.AbstractUIAppModel;
import nc.vo.pub.BusinessException;
import nc.vo.rm.active.ActiveJobVO;
import nc.vo.rm.active.ActiveVO;
import nc.vo.rm.active.AggActiveVO;
import nc.vo.uif2.LoginContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;



public class StartActiveAction
  extends HrAction
{
  public StartActiveAction()
  {
    putValue("Code", "StartActive");
    setBtnName(ResHelper.getString("6021active", "06021active0004"));
  }
  
  protected boolean isActionEnable()
  {
    if ((StringUtils.isEmpty(getModel().getContext().getPk_org())) || (getModel().getSelectedData() == null))
      return false;
    AggActiveVO aggvo = (AggActiveVO)getModel().getSelectedData();
    ActiveVO vo = (ActiveVO)aggvo.getParentVO();
    if ((vo.getActivestate() == null) || (vo.getActivestate().intValue() == 0))
      return true;
    return false;
  }
  
  public void doAction(ActionEvent e) throws Exception
  {
    checkDataPermission();
    AggActiveVO aggvo = (AggActiveVO)getModel().getSelectedData();
    ActiveJobVO[] jobvos = (ActiveJobVO[])aggvo.getTableVO("sub_jobinfo");
    if (ArrayUtils.isEmpty(jobvos))
      throw new BusinessException(ResHelper.getString("6021active", "06021active0005"));
    ((RMActiveAppModel)getModel()).startActive(aggvo);
  }
}
