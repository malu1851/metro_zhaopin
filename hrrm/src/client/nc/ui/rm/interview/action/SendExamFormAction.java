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
import nc.itf.hr.message.IHRMessageSend;
import nc.itf.rm.IInterviewManageService;
import nc.itf.rm.IRMPsndocQueryService;
import nc.pubitf.rbac.IUserPubService;
import nc.ui.am.common.XlsFileFilter;
import nc.ui.hr.notice.action.SendMessageAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.rm.interview.model.InterviewAppModel;
import nc.ui.rm.interview.model.PassInterviewAppModel;
import nc.ui.rm.interview.model.WaitInterviewAppModel;
import nc.ui.rm.psndoc.model.RMPsndocAppModel;
import nc.ui.rm.pub.view.RMSendMessageDialog;
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

public class SendExamFormAction extends SendMessageAction {

	public SendExamFormAction() {
		setCode("SendInvite");
		setBtnName("�������֪ͨ");

	}

	public void doAction(ActionEvent e) throws Exception {

		
		Object[] selectDatas = getApplyModel().getSelectedOperaDatas();		
		if (selectDatas == null || selectDatas.length == 0) {
			
			throw new BusinessException("��ѡ����������ԱΪ�գ����ܷ������֪ͨ");
		}
		List<String> filePaths = getFilePaths();//��ȡ����	;
		for (Object selectData : selectDatas) {
			AggInterviewVO aggvo = (AggInterviewVO) selectData;
			if ((aggvo == null)
					|| (StringUtils.isEmpty(aggvo.getInterviewVO()
							.getPk_psndoc()))) {
				throw new BusinessException("��ѡ����������Ա��Ϣ��Ч�����ܷ������֪ͨ");
			}
			String pk_psndoc = aggvo.getInterviewVO().getPk_psndoc();
			AttachFileUtils AttachFileUtils = new AttachFileUtils();
			IInterviewManageService messageSendServer = (IInterviewManageService) NCLocator
					.getInstance().lookup(IInterviewManageService.class);
			HRBusiMessageVO messageVO = new HRBusiMessageVO();
			messageVO.setBillVO(aggvo.getInterviewVO());
			messageVO.setMsgrescode("602115");
			messageVO.setPkorgs(new String[] { getModel().getContext()
					.getPk_org() });
			String email = getPsndocForSendMes(pk_psndoc).getEmail();
			if (!StringUtils.isEmpty(email))
				messageVO.setReceiverEmails(new String[] { email });
			String mobile = getPsndocForSendMes(pk_psndoc).getMobile();
			if (!StringUtils.isEmpty(mobile)) {
				messageVO.setReceiverMobiles(new String[] { mobile });
			}
			AttachFileUtils.sendBuziMessage_RequiresNew(messageVO, filePaths);
			//aggvo = ((WaitInterviewAppModel) getModel()).sendMessageFlag(aggvo);
			//((WaitInterviewAppModel) getModel()).directlyUpdate(aggvo);
			putValue("message_after_action","�������֪ͨ�ɹ�");
		}
		
	}


	private RMPsndocVO getPsndocForSendMes(String pk_psndoc) {
		try {
			AggRMPsndocVO aggpsnvo = ((IRMPsndocQueryService) NCLocator
					.getInstance().lookup(IRMPsndocQueryService.class))
					.queryByPK(pk_psndoc);

			if (aggpsnvo != null)
				return aggpsnvo.getPsndocVO();
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		return null;
	}

	public InterviewAppModel getApplyModel() {
		return (InterviewAppModel) getModel();
	}

	public  List<String> getFilePaths(){
		   //String[] list = new String[1];
		   List<String> list = new ArrayList<String>();
		   int index=MessageDialog.showOkCancelDlg(getEntranceUI(),"�ϴ�������ʾ","�Ƿ��ϴ�����");
		   if(index==1){
			   list = fileChooser(list);			   		   
		   }
		return list;		   	  	  
	  }	  
	  private List<String> fileChooser(List<String> list){
		  
		  /**
			 * 1����ȡ���ļ�ѡ�����Ի���ģ�͡������ġ���ڣ��ļ���ť==�ļ�ѡ������======������ļ�ѡ�����Ի���
			 * 
			 */
			File file = null;
			if (getUIFileChooser().showDialog(
					getModel().getContext().getEntranceUI(), "ѡ���ļ�") == JFileChooser.APPROVE_OPTION) {
				file = getUIFileChooser().getSelectedFile(); // �ļ�����ѡ����ѡ���ļ�				
				if (file == null || file.getName().trim().length() == 0) { // ���ļ�Ϊ�ջ����ֳ���Ϊ0������ѡ���ļ���ʾ
					// ������Ϣ��ʾ����ʾ����
					MessageDialog
							.showErrorDlg(
									getModel().getContext().getEntranceUI(),
									nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
											.getStrByID("ampub_0",
													"04501000-0382")/* @res "����" */,
									"��ѡ��Ҫ������ļ�!");
					 return list;
				}	
				list.add(file.getPath());
 		    int index =MessageDialog.showYesNoCancelDlg(getEntranceUI(),"�ϴ�������ʾ","�Ƿ�����ϴ�����");
				if(index==4){					
					fileChooser(list);//�ݹ��ȡ��������									
				}else if(index==2){
					 list.clear();										
				}
		     } else {				
				return list;				
		     }		  
		  return list;		  		  
	  }
	  	  	    	  
	  /**
		 * �ļ�ѡ����
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
