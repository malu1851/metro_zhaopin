package nc.ui.rm.publish.action;

import java.awt.event.ActionEvent;

import nc.bs.dao.BaseDAO;
import nc.bs.uif2.validation.ValidationException;
import nc.hr.utils.ResHelper;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.hr.util.HrDataPermHelper;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.rm.interview.model.WaitInterviewAppModel;
import nc.ui.rm.publish.model.PublishAppModel;
import nc.ui.rm.publish.view.PublishCardForm;
import nc.ui.uif2.model.AbstractUIAppModel;
import nc.vo.pub.BusinessException;
import nc.vo.rm.publish.AggPublishVO;
import nc.vo.rm.publish.PublishJobVO;
import nc.vo.rm.publish.PublishPlaceVO;
import nc.vo.rm.publish.PublishStatusEnum;
import nc.vo.rm.publish.UnPublishJobValidator;
import nc.vo.uif2.LoginContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class PublishPathAction
  extends HrAction
{
	
  public PublishPathAction()
  {
    putValue("Code", "unpublish");
    setBtnName(ResHelper.getString("6021publish", "һ�������ط�"));
  } 

  public void doAction(ActionEvent e) throws Exception {
	  Object[] objp = ((PublishAppModel)getModel()).getSelectedOperaDatas(); 
	  if (ArrayUtils.isEmpty(objp)){
	         throw new BusinessException(ResHelper.getString("6021publish", "δѡ��ְλ��Ŀ���޷�ѡ�񷢲��ط�"));}	  
	  int num = 0;
	  Object[]  objs = new Object[2];
	  objs[0] = "�����Ƹ";
	  objs[1] = "У԰��Ƹ"; 	  
      Object obj=MessageDialog.showSelectDlg(this.getEntranceUI(),1,"ְλ�����ط�","��ѡ��ְλ�����ط�", objs,3);  
	  if("�����Ƹ".equals(obj)){		  
		  num = 1;	  
	  }else{		  
		  num = 2;
	  }	  
      if(num!=0){
          BaseDAO dao=new BaseDAO();
          for(Object objt:objp){
    	  AggPublishVO aggvo = (AggPublishVO)objt;
    	  PublishJobVO publishJobVO  = (PublishJobVO) aggvo.getParentVO();
    	  PublishPlaceVO[] publishPlaceVOs=aggvo.getPublishPlaceVOs();
    	  if(ArrayUtils.isEmpty(publishPlaceVOs)){
    		 PublishPlaceVO  publishPlaceVO=  new PublishPlaceVO();
    		 publishPlaceVO.setPk_publishjob(publishJobVO.getPk_publishjob());
    		 publishPlaceVO.setPlace(num); 
    		 PublishPlaceVO[] publishPlaceVOss = new PublishPlaceVO[1];
    		 publishPlaceVOss[0]=publishPlaceVO;
    		 aggvo.setPublishPlaceVOs(publishPlaceVOss);
    		 ((PublishAppModel)getModel()).update(aggvo);
    	  }else{ 	
    		 PublishPlaceVO  publishPlaceVO=  new PublishPlaceVO();
    		 for(int i = 0;i<publishPlaceVOs.length;i++){  			  
    			  publishPlaceVO.setPk_publishjob(publishJobVO.getPk_publishjob());
    	    	  publishPlaceVO.setPlace(num); 
    	    	  publishPlaceVOs[i]=publishPlaceVO;  			    			  
    		  }
    		  aggvo.setPublishPlaceVOs(publishPlaceVOs);
    		  ((PublishAppModel)getModel()).update(aggvo);     		  
    	  }	 
      }	  
    }  
  }
}
