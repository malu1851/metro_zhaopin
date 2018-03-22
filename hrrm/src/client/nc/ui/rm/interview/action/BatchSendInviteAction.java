package nc.ui.rm.interview.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import jxl.Workbook;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.itf.rm.IInterviewManageService;
import nc.itf.rm.IRMPsndocQueryService;
import nc.pubitf.rbac.IUserPubService;
import nc.ui.am.common.XlsFileFilter;
import nc.ui.hr.notice.action.SendMessageAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.bill.BillItem;
import nc.ui.pubapp.uif2app.ShowDialogExceptionHandler;
import nc.ui.rm.interview.model.WaitInterviewAppModel;
import nc.ui.rm.psndoc.model.RMPsndocAppModel;
import nc.ui.rm.pub.view.RMSendMessageDialog;
import nc.ui.uif2.model.BillManageModel;
import nc.util.rm.AttachFileUtils;
import nc.vo.hr.message.HRBusiMessageVO;
import nc.vo.ml.LanguageVO;
import nc.vo.ml.MultiLangContext;
import nc.vo.pub.BusinessException;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsndocVO;
import nc.vo.rm.pub.RMNoticeMessageVO;
import nc.vo.sm.UserVO;

/***
 * 批量发送带附件邮件
 * @author 马鹏鹏
 *
 */
public class BatchSendInviteAction extends SendMessageAction {
	  private RMSendMessageDialog sendMessageDialog;
	  private RMNoticeMessageVO hrNoticeMessageVO;
	  
	  public BatchSendInviteAction()
	  {
		    setCode("SendInvite");
		    String name = ResHelper.getString("6021interview", "06021interview0023");		    
		    setBtnName(name);
		    putValue("ShortDescription", name);
	  }
	  
	  public void doAction(ActionEvent e) throws Exception
	  {		
		
		Object[] selectDatas = getApplyModel().getSelectedOperaDatas();
	
	    if (selectDatas==null||selectDatas.length == 0) {
		      throw new BusinessException(ResHelper.getString("6021interview", "06021interview0024"));
		 }
	    List<String> filePaths = getFilePaths();//添加附件
		for(Object selectData:selectDatas){
			 AggInterviewVO aggvo =(AggInterviewVO)selectData;
			 if ((aggvo == null) || (StringUtils.isEmpty(aggvo.getInterviewVO().getPk_psndoc()))) {
			      throw new BusinessException(ResHelper.getString("6021interview", "06021interview0024"));
			    }
			    String pk_psndoc = aggvo.getInterviewVO().getPk_psndoc();			    
			    AttachFileUtils AttachFileUtils = new AttachFileUtils();
			    HRBusiMessageVO messageVO = new HRBusiMessageVO();
			    messageVO.setBillVO(aggvo.getInterviewVO());
			    messageVO.setMsgrescode("602107");
			    messageVO.setPkorgs(new String[] { getModel().getContext().getPk_org() });
			    String email = getPsndocForSendMes(pk_psndoc).getEmail();
			    if (!StringUtils.isEmpty(email))
			      messageVO.setReceiverEmails(new String[] { email });
			    String mobile = getPsndocForSendMes(pk_psndoc).getMobile();
			    if (!StringUtils.isEmpty(mobile)) {
			      messageVO.setReceiverMobiles(new String[] { mobile });
			    }
			    AttachFileUtils.sendBuziMessage_RequiresNew(messageVO,filePaths);
			    aggvo = ((WaitInterviewAppModel)getModel()).sendMessageFlag(aggvo);
			    ((WaitInterviewAppModel)getModel()).directlyUpdate(aggvo);
		    putValue("message_after_action", ResHelper.getString("6021interview", "06021interview0025"));
		}	  	  		  
		    
	  }
	  
	  

	  private RMPsndocVO getPsndocForSendMes(String pk_psndoc)
	  {
	    try
	    {
	      AggRMPsndocVO aggpsnvo = ((IRMPsndocQueryService)NCLocator.getInstance().lookup(IRMPsndocQueryService.class)).queryByPK(pk_psndoc);
	      
	      if (aggpsnvo != null)
	        return aggpsnvo.getPsndocVO();
	    } catch (BusinessException e) {
	      Logger.error(e.getMessage(), e);
	    }
	    return null;
	  }
	  	     
	  public WaitInterviewAppModel getApplyModel() { return (WaitInterviewAppModel)getModel(); }	  
	  
	  public  List<String> getFilePaths(){
		   //String[] list = new String[1];
		   List<String> list = new ArrayList<String>();
		   int index=MessageDialog.showOkCancelDlg(getEntranceUI(),"上传附件提示","是否上传附件");
		   if(index==1){
			   list = fileChooser(list);			   		   
		   }
		return list;		   	  	  
	  }	  
	  private List<String> fileChooser(List<String> list){
		  
		  /**
			 * 1、获取的文件选择器对话框（模型、上下文、入口，文件按钮==文件选择器）======导入的文件选择器对话框
			 * 
			 */
			File file = null;
			if (getUIFileChooser().showDialog(
					getModel().getContext().getEntranceUI(), "选择文件") == JFileChooser.APPROVE_OPTION) {
				file = getUIFileChooser().getSelectedFile(); // 文件查找选择器选择文件				
				if (file == null || file.getName().trim().length() == 0) { // 若文件为空或名字长度为0，做出选择文件提示
					// 错误消息提示框，提示口令
					MessageDialog
							.showErrorDlg(
									getModel().getContext().getEntranceUI(),
									nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
											.getStrByID("ampub_0",
													"04501000-0382")/* @res "错误" */,
									"请选择要导入的文件!");
					 return list;
				}	
				list.add(file.getPath());
      		    int index =MessageDialog.showYesNoCancelDlg(getEntranceUI(),"上传附件提示","是否继续上传附件");
				if(index==4){					
					fileChooser(list);									
				}else if(index==2){
					 list.clear();										
				}
		     } else {				
				return list;				
		     }		  
		  return list;		  		  
	  }
	  	  	    	  
	  /**
		 * 文件选择器
		 */
		private JFileChooser fileChooser = null;

		private JFileChooser getUIFileChooser() {
			if (fileChooser == null) {
				fileChooser = new JFileChooser();
				XlsFileFilter filter = new XlsFileFilter();		
				filter.setDescription(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("ampub_0", "04501000-0468"));
			
			}
			return fileChooser;
		}
	  
	  
	  
}
