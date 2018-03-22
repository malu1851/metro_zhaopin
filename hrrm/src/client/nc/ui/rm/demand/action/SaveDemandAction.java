package nc.ui.rm.demand.action;

import java.awt.event.ActionEvent;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.rm.IDemandQueryService;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.JdbcSession;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.uap.lfw.core.AppInteractionUtil;
import nc.ui.hr.uif2.action.SaveAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.rm.demand.model.DemandAppModel;
import nc.ui.trade.business.HYPubBO_Client;
import nc.ui.uif2.IShowMsgConstant;
import nc.vo.pub.BusinessException;
import nc.vo.rm.demand.AggRMDemandVO;
import nc.vo.rm.demand.DemandJobVO;
import nc.vo.rm.demand.DemandVO;
//保存前增加编制校验   马鹏鹏
public class SaveDemandAction extends SaveAction
{	  

  IDemandQueryService  is =NCLocator.getInstance().lookup(IDemandQueryService.class);
  public SaveDemandAction() {}
  
  public void doAction(ActionEvent evt) throws Exception
  {
	  
	    Object objValue = super.getEditor().getValue();
		
		AggRMDemandVO aggRMDemandVO = (AggRMDemandVO) objValue;
		DemandVO demandVo = aggRMDemandVO.getDemandVO();
		// 获取组织
		String pk_deptorg = demandVo.getPk_rmorg();
		// 获取部门
		String pk_rmdept = demandVo.getPk_rmdept();

		/**
	       * 保存前判断
	       * 如果是集团：查询出部门中层副职以下人员
	       * 如果是分公司：查询出公司所有人员
	       * 最后计算是否超编 
	       */  
	       //共同代码
	       StringBuffer cmsb =new StringBuffer();
	       cmsb.append("(select pk_dimension from ");
	       cmsb.append("(select pk_dimension, dimension_year from hrp_dimension where pk_dim_doc =");
	       cmsb.append("(select pk_dimension from hrp_dimension where pk_org =");
	       cmsb.append("(select pk_org from org_adminorg where pk_adminorg = '"+pk_deptorg+"')");
	       cmsb.append("and dimension_name = '职级编制维度') and dimension_state = 1");
	       cmsb.append("order by dimension_year desc) where rownum = 1)");

	       StringBuffer sb =null;
	       //只有集团组织存在中层正职人员
	       if("0001E410000000004QO4".equalsIgnoreCase(pk_deptorg)){
	           //查询代码
	           sb = new StringBuffer();
	           sb.append("select budget_leftover from HRP_POSTBUDGET where pk_post =");
	           sb.append("(select pk_dimensiondef from hrp_dimensiondef where pk_dimension ="); 
	           sb.append(cmsb);
	           sb.append("and pk_dimdefdoc = (select pk_dimensiondef from hrp_dimensiondef where dimensiondef_name = '中层副职以下人员编制'))");
	           sb.append("and pk_dept_budget =");
	           sb.append("(select pk_dept_budget from hrp_deptbudget where pk_dept = '"+pk_rmdept+"'");
	           sb.append("and pk_dimension =");
	           sb.append(cmsb);
	           sb.append(")");	
	       }else{
	    	   //查询代码
	           sb = new StringBuffer();
	           sb.append("select budget_leftover from hrp_orgbudget_sub where pk_dimensiondef =");
	           sb.append("(select pk_dimensiondef from hrp_dimensiondef where pk_dimension = "); 
	           sb.append(cmsb);
	           sb.append("and pk_dimdefdoc = (select pk_dimensiondef from hrp_dimensiondef where dimensiondef_name = '中层副职以下人员编制'))");
	           sb.append("and pk_org_budget =");
	           sb.append("(select pk_org_budget from hrp_orgbudget where pk_org ='"+pk_deptorg+"'");
	           sb.append("and pk_dimension =");
	           sb.append(cmsb);
	           sb.append(")");	   	      	      	   
	        } 	    	 
	    	   String obj= null;
			try {			
				obj = is.getBudget(sb.toString());						
			} catch (Exception e) {
				e.getStackTrace();
			}
			int leftover =0;
	        if(obj!=null&&!"查询编制异常".equalsIgnoreCase(obj)){
				leftover = Integer.parseInt(obj);
			}			
//			if (leftover >= 0) {
//				if(!AppInteractionUtil.showConfirmDialog("编制提示", "本部门超（缺）编人数为:" + leftover
//						+ ",超编,是否继续保存")){
//					return;
//				}
//				//throw new BusinessException("本部门超（缺）编人数为:" + leftover+ ",超编,不允许保存");
//			}
			DemandJobVO[] demandJobVOs = aggRMDemandVO.getDemandJobVO();	             	        
			int num = 0;
//			if (demandJobVOs==null||demandJobVOs.length <= 0) {												
//				throw new BusinessException("未添加招聘职位,不允许保存");
//			} else {
//				for (int i = 0; i < demandJobVOs.length; i++) {
//					num += demandJobVOs[i].getRmnum();
//				}									
//			}
			if(demandJobVOs!=null){
				for (int i = 0; i < demandJobVOs.length; i++) {
					num += demandJobVOs[i].getRmnum();
				}								
			}
			if(num+leftover>0){					
				if(MessageDialog.showOkCancelDlg(getEntranceUI(), "编制提示", "本部门超（缺）编人数为:" + leftover
						+ ",超编,是否继续保存")!=1){
					return;
				}
				
			}		
    
        super.doAction(evt);
        putValue("message_after_action", IShowMsgConstant.getSaveSuccessInfo());
  }
}
