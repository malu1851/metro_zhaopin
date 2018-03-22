package nc.ui.rm.interview.action;

import java.awt.event.ActionEvent;
import java.util.List;
import nc.hr.utils.ResHelper;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.rm.interview.model.FailInterviewAppModel;
import nc.ui.rm.interview.model.InterviewAppModel;
import nc.ui.rm.pub.RMModelHelper;
import nc.ui.uif2.IShowMsgConstant;
import nc.ui.uif2.model.AbstractUIAppModel;
import nc.vo.pub.BusinessException;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.uif2.LoginContext;

public class StopInterviewAction
  extends HrAction
{
  private FailInterviewAppModel failModel;

public InterviewAppModel getAppModel() {
	return (InterviewAppModel)getModel();
}


public StopInterviewAction()
  {
    setCode("StopInterview");
    String name = ResHelper.getString("6021interview", "06021interview0030");
    setBtnName(name);
    putValue("ShortDescription", name);
  }
  
  public void doAction(ActionEvent e)
    throws Exception
  {
    int ret = MessageDialog.showYesNoDlg(getContext().getEntranceUI(), ResHelper.getString("6021pub", "06021pub0040", new String[] { getBtnName() }), ResHelper.getString("6021interview", "06021interview0031"));
    if (4 != ret)
    {
      putValue("message_after_action", IShowMsgConstant.getCancelInfo());
      return;
    }
    
   //获取复选框选中的数据
    Object[]  selectDatas = getAppModel().getSelectedOperaDatas();
    
    if(selectDatas==null){
    	
    	throw new BusinessException("请选择人员");
    }
    
    for(Object selectData: selectDatas){
    AggInterviewVO aggvo = (AggInterviewVO)selectData;
    aggvo = ((InterviewAppModel)getModel()).stopInterview(aggvo);
    getModel().initModel(((InterviewAppModel)getModel()).getData().toArray());
    ((InterviewAppModel)getModel()).directlyDelete(aggvo);
    RMModelHelper.directMultiAdd(getFailModel(), new Object[] { aggvo });
    
    }
    //((InterviewAppModel)getModel()).directlyDelete(aggvo);
    putValue("message_after_action", ResHelper.getString("6001uif2", "06001uif20010", new String[] { getBtnName() }));
  }
  
  protected boolean isActionEnable()
  {
    if (getModel().getSelectedData() == null) {
      return false;
    }
    AggInterviewVO aggvo = (AggInterviewVO)getModel().getSelectedData();
    if (aggvo == null) {
      return false;
    }
    if (aggvo.getInterviewVO() == null) {
      return false;
    }
    return super.isActionEnable();
  }
  
  public FailInterviewAppModel getFailModel()
  {
    return this.failModel;
  }
  
  public void setFailModel(FailInterviewAppModel failModel)
  {
    this.failModel = failModel;
  }
}
