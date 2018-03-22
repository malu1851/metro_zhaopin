package nc.ui.rm.psndoc.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.uap.sfapp.util.SFAppServiceUtil;
import nc.bs.uif2.validation.Validator;
import nc.hr.utils.ResHelper;
import nc.itf.uap.IUAPQueryBS;
import nc.itf.uap.sf.IConfigFileService;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.BeanProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.rm.pub.view.HireItemCardForm;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.pub.BusinessException;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsndocVO;
import nc.vo.sm.config.Account;

import org.apache.commons.lang.ArrayUtils;

/**
 * 简历导出类
 * @author lichao  20170407
 *
 */
public class DcjlAction extends HrAction{
  
	
	  private HireItemCardForm billform;
	  private Validator hireValidator;
	  private int status;
	  public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Validator getHireValidator()
	  {
	    return this.hireValidator;
	  }
	  
	  public void setHireValidator(Validator hireValidator)
	  {
	    this.hireValidator = hireValidator;
	  }
	  
	  public HireItemCardForm getBillform()
	  {
	    return this.billform;
	  }	  
	  public void setBillform(HireItemCardForm billform)
	  {
	    this.billform = billform;
	    getModel().removeAppEventListener(billform);
	  }
	  
	  public DcjlAction()
	  {
		  setCode("Dcjl");
		  String name = "导出简历";
		  setBtnName(name);
		  putValue("ShortDescription", name);
	  }
	  
	  @SuppressWarnings({ "restriction", "unchecked", "rawtypes" })
	public void doAction(ActionEvent e)throws Exception{
		  File[] roots =File.listRoots();
		  Object[]  objs = new Object[roots.length];
		  for(int i=0;i<roots.length;i++){
			  objs[i]=roots[i].toString();  			  
		  } 	
		  Object object =MessageDialog.showSelectDlg(this.getEntranceUI(),"盘符选择", "请选择简历导出盘符(默认C盘)：", objs,roots.length);		  
		  if(object==null){
			  return;		  		  
		  }	
		  String panfu  = object.toString();
		  Object[] selectDatas = ((BillManageModel)getModel()).getSelectedOperaDatas();
	      if (ArrayUtils.isEmpty(selectDatas)) {
	        throw new BusinessException("请选择人员！!");
	      }else{
	    	  IUAPQueryBS bs = NCLocator.getInstance().lookup(IUAPQueryBS.class);
	    	  String sql_org = "select code,name,pk_adminorg from org_adminorg where nvl(dr,0)=0";
	    	  List list_org = (List) bs.executeQuery(sql_org.toString(), new ArrayListProcessor());
	    	  Map map_org = new HashMap();//应聘组织
	    	  if(list_org.size()>0){
	    		  for(int i=0;i<list_org.size();i++){
	    			  Object[] obj = (Object[]) list_org.get(i);
	    			 // map_org.put(obj[2].toString(), obj[0].toString()+obj[1].toString());
	    			  map_org.put(obj[2].toString(),obj[1].toString());
	    		  }
	    	  }
	    	  String sql_dept = "select code,name,pk_dept from org_dept where nvl(dr,0)=0";
	    	  List list_dept = (List) bs.executeQuery(sql_dept, new ArrayListProcessor());
	    	  Map map_dept = new HashMap();//应聘部门
	    	  if(list_dept.size()>0){
	    		  for(int i=0;i<list_dept.size();i++){
	    			  Object[] obj = (Object[]) list_dept.get(i);
	    			  map_dept.put(obj[2].toString(),obj[1].toString());
	    		  }
	    	  }
	    	  StringBuffer sb_job = new StringBuffer();
	    	  sb_job.append("select j.code,j.name,p.pk_publishjob from rm_publish p ");
	    	  sb_job.append("inner join rm_job j on p.pk_job = j.pk_job and nvl(j.dr,0)=0 ");
	    	  sb_job.append("where nvl(p.dr,0)=0 ");
	    	  List list_job = (List) bs.executeQuery(sb_job.toString(), new ArrayListProcessor());
	    	  Map map_job = new HashMap();//应聘职位
	    	  if(list_job.size()>0){
	    		  for(int i=0;i<list_job.size();i++){
	    			  Object[] obj = (Object[]) list_job.get(i);
	    			  map_job.put(obj[2].toString(),obj[1].toString());
	    		  }
	    	  }
	    	  String sql_defdoc = "select name,pk_defdoc from bd_defdoc where nvl(dr,0)=0";
	    	  List list_defdoc = (List) bs.executeQuery(sql_defdoc, new ArrayListProcessor());
	    	  Map map_defdoc = new HashMap();//自定义档案
	    	  for(int i=0;i<list_defdoc.size();i++){
	    		  Object[] obj = (Object[]) list_defdoc.get(i);
	    		  map_defdoc.put(obj[1].toString(), obj[0].toString());
	    	  }
	    	  String sql_bdregion = "select name,pk_region from bd_region where nvl(dr,0)=0";
	    	  List list_region= (List)bs.executeQuery(sql_bdregion, new ArrayListProcessor());
	    	  Map map_region = new HashMap();//行政区划    	  	     
	    	  for(int i=0;i<list_region.size();i++){
	    		  Object[] obj = (Object[]) list_region.get(i);
	    		  map_region.put(obj[1].toString(), obj[0].toString());
	    	  }
	    	  
    	      Map<String,Object> map = toMap(selectDatas,bs);
    
	    	  for(int i=0;i<selectDatas.length;i++){	
	    			AggRMPsndocVO aggvo = (AggRMPsndocVO)selectDatas[i];
	    			//setPhoto(aggvo,bs);
	    			Object photo = map.get(aggvo.getPsndocVO().getPk_psndoc());
	    			if(photo!=null){
	    				aggvo.getPsndocVO().setPhoto(photo);    				
	    			}else{
	    				aggvo=setPhoto(aggvo,bs);	    				
	    			}
		    		DcjlExcelTool exceltool = new DcjlExcelTool();		    	
		    		exceltool.newExcel(aggvo,map_org,map_dept,map_job,map_defdoc,status,panfu,map_region);	  
	    	  }
	    	  MessageDialog.showHintDlg(this.getEntranceUI(), "提示", "简历导出成功！");
	      }
	  }
	  
	  public  Map<String,Object>  toMap(Object[] selectDatas,IUAPQueryBS bs) throws BusinessException{
		   Map<String,Object> map= new HashMap<String,Object>();
		   
		   int sum = selectDatas.length/100;
		   int dex = selectDatas.length%100;
		   StringBuffer sb = null;		
		   for(int i = 0;i<=sum;i++){
			   sb = new StringBuffer("'1'");			 
			   Object[] obj = null;
			   if(i!=sum){
				    obj = Arrays.copyOfRange(selectDatas, i*100, (i+1)*100-1);  
				   
			   }else{
				   
				   obj = Arrays.copyOfRange(selectDatas, i*100, selectDatas.length-1);  
			   }
			   for(int j=0;j<obj.length;j++){
				   AggRMPsndocVO aggvo = (AggRMPsndocVO)obj[j];
				   sb.append(","+"'"+aggvo.getPsndocVO().getPk_psndoc()+"'");	
				   
			   }
			   String sql_photo = "select rp.pk_psndoc,rp.photo from rm_psndoc rp where rp.pk_psndoc in ("+sb.toString()+") and  nvl(dr,0) = 0 ";
			   List<RMPsndocVO> psnvo_list= (List<RMPsndocVO>)bs.executeQuery(sql_photo, new BeanListProcessor(RMPsndocVO.class));
			   if(psnvo_list!=null&&psnvo_list.size()>0){
				 for(int m=0;m<psnvo_list.size();m++){
					 map.put(psnvo_list.get(m).getPk_psndoc(),psnvo_list.get(m).getPhoto());					 
				 }  				   
			   }
		   }  
		   return map;  
	  }
	  
	  
	  
	  
	  public AggRMPsndocVO  setPhoto(AggRMPsndocVO aggvo,IUAPQueryBS bs) throws Exception{
		  //照片从数据库查询  20180305  mpp
    	  String sql_photo = "select rp.pk_psndoc,rp.photo from rm_psndoc rp where rp.pk_psndoc = '"+aggvo.getPsndocVO().getPk_psndoc()+"' and  nvl(dr,0) = 0 ";
    	  RMPsndocVO psnvo= (RMPsndocVO)bs.executeQuery(sql_photo, new BeanProcessor(RMPsndocVO.class));
    	  aggvo.getPsndocVO().setPhoto(psnvo.getPhoto());	  
		  return aggvo;
	  }
	  
	  
	 
}
