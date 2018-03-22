package nc.ui.rm.interview.action;

import java.awt.event.ActionEvent;
import nc.hr.utils.ResHelper;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.rm.interview.model.InterviewAppModel;
import nc.ui.rm.interview.model.WaitInterviewAppModel;
import nc.ui.rm.pub.RMModelHelper;
import nc.ui.uif2.model.AbstractUIAppModel;
import nc.vo.pub.BusinessException;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.interview.InterviewPlanVO;
import nc.vo.rm.interview.InterviewVO;
import nc.vo.uif2.LoginContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;



public class StartInterviewAction
  extends HrAction
{
  private InterviewAppModel interviewModel;
  
  public StartInterviewAction()
  {
    setCode("StartInterview");
    String name = ResHelper.getString("6021interview", "06021interview0027");
    
    setBtnName(name);
    putValue("ShortDescription", name);
  }
 /**
  * 复选框多选启动面试
  * 马鹏鹏 
  *  
  */
  public void doAction(ActionEvent e)
    throws Exception
  {
    int ret = MessageDialog.showYesNoDlg(getContext().getEntranceUI(), ResHelper.getString("6021pub", "06021pub0040", new String[] { getBtnName() }), ResHelper.getString("6021interview", "06021interview0028"));   
    if (4 != ret) {
    	
      setCancelMsg();
      return;
    }
    
    Object[] selectDatas = getApplyModel().getSelectedOperaDatas();
	if (selectDatas==null||selectDatas.length == 0) {
	      throw new BusinessException("未选择人员，无法查看信息");
	} 
	for(Object selectData:selectDatas){
		AggInterviewVO aggvo =(AggInterviewVO)selectData;
       // AggInterviewVO aggvo = (AggInterviewVO)getModel().getSelectedData();
		if (aggvo == null)
		      return;
		    aggvo = ((WaitInterviewAppModel)getModel()).startInterivew(aggvo);		    
		    RMModelHelper.directMultiAdd(getInterviewModel(), new Object[] { aggvo });  	
    }	
	((WaitInterviewAppModel)getModel()).directlyDelete(selectDatas);
    putValue("message_after_action", ResHelper.getString("6001uif2", "06001uif20010", new String[] { getBtnName() }));
  }
  
  public WaitInterviewAppModel getApplyModel() { return (WaitInterviewAppModel)getModel(); }

  protected boolean isActionEnable()
  {
    if (StringUtils.isEmpty(getModel().getContext().getPk_org()))
      return false;
    if (getModel().getSelectedData() == null)
      return false;
    AggInterviewVO aggvo = (AggInterviewVO)getModel().getSelectedData();
    InterviewVO interviewVO = aggvo.getInterviewVO();
    if (interviewVO == null)
      return false;
    if ((StringUtils.isEmpty(interviewVO.getPk_reg_org())) || (StringUtils.isEmpty(interviewVO.getPk_reg_dept())) || (StringUtils.isEmpty(interviewVO.getPk_psndoc_job())))
    {

      return false;
    }
    //在不添加面试轮次的情况下,启动面试
    /*InterviewPlanVO[] planVOs = aggvo.getInterviewPlanVOs();
    if (ArrayUtils.isEmpty(planVOs))
      return false;
    for (InterviewPlanVO planVO : planVOs) {
      if ((StringUtils.isEmpty(planVO.getInterviewer())) || (StringUtils.isEmpty(planVO.getPk_evaitem())) || (planVO.getViewertype() == null) || (StringUtils.isEmpty(planVO.getViewertype().toString())) || (StringUtils.isEmpty(planVO.getPk_viewer_dept())))
      {

        return false;
      }
    }*/
    return super.isActionEnable();
  }
  
  public InterviewAppModel getInterviewModel() {
    return this.interviewModel;
  }
  
  public void setInterviewModel(InterviewAppModel interviewModel) {
    this.interviewModel = interviewModel;
  }
}
