package nc.ui.rm.psndoc.action;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;

import com.ibm.db2.jcc.sqlj.e;

import nc.bs.framework.common.NCLocator;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.lang.UFDate;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMEduVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.RMPsnWorkVO;
import nc.vo.rm.psndoc.RMPsndocVO;

/**
 * 导出汇总简历类
 * @author lichao 20170421
 *
 */
@SuppressWarnings({ "restriction", "serial" })
public class DcjlHzAction extends HrAction {
	
	private int status;
	  public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public DcjlHzAction()
	  {
		  setCode("DcjlHz");
		  String name = "导出汇总简历";
		  setBtnName(name);
		  putValue("ShortDescription", name);
	  }
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void doAction(ActionEvent e) throws Exception {
		
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
	  		  String date = (new UFDate()).getYear()+"-"+(new UFDate()).getMonth()+"-"+(new UFDate()).getDay();
			  String dirNameRoot = panfu+"简历汇总导出"+ File.separator;
			  String schlName ="";
			  String scoiName =""; 
			  String othName ="";
	  		  if(status==1){
	  			  dirNameRoot += "应聘登记人员简历汇总"+ File.separator+date+File.separator;				   
			  }else if(status==2){
				  dirNameRoot += "初选通过人员简历汇总"+ File.separator+date+File.separator;			   
			  }
	  		  Map  PsnvoMap= getPsnvoMap(selectDatas);
	  		  Set keySet=PsnvoMap.keySet();
	  		  Iterator itt = keySet.iterator();
	  		  while(itt.hasNext()){	 
	  			  Object keyObj=(String)itt.next();
	  			 if(keyObj.equals("1")){
	  				HSSFWorkbook workbook = new HSSFWorkbook();
	  	  		    HSSFSheet sheet = workbook.createSheet("校园简历汇总");
	  				schlName =  dirNameRoot+"校园招聘简历汇总"+File.separator;
	  				List<AggRMPsndocVO>  aggList = (List<AggRMPsndocVO>)PsnvoMap.get("1");
	  				createSchlExcel(sheet,workbook,aggList,map_org,map_dept,map_job,map_defdoc,map_region);
	  				File file = new File(schlName);
	  				file.mkdirs();
	  			    BufferedWriter write = new BufferedWriter(new FileWriter(schlName+"简历汇总.xls")); 
	  			    write.close();
	  				OutputStream o = new FileOutputStream(schlName+"简历汇总.xls");			
	  				workbook.write(o);
	  				o.flush();
	  				o.close();
	  			 }else if(keyObj.equals("2")){
	  				HSSFWorkbook workbook = new HSSFWorkbook();
	  	  		    HSSFSheet sheet = workbook.createSheet("社会简历汇总");
	  				scoiName =  dirNameRoot+"社会招聘简历汇总"+File.separator;
	  				List<AggRMPsndocVO>  aggList = (List<AggRMPsndocVO>)PsnvoMap.get("2");
	  				//createSchlExcel(sheet,workbook,aggList,map_org,map_dept,map_job,map_defdoc,map_region);
	  				createScoiExcel(sheet,workbook,aggList,map_org,map_dept,map_job,map_defdoc,map_region);
	  				File file = new File(scoiName);
	  				file.mkdirs();
	  			    BufferedWriter write = new BufferedWriter(new FileWriter(scoiName+"简历汇总.xls")); 
	  			    write.close();
	  				OutputStream o = new FileOutputStream(scoiName+"简历汇总.xls");			
	  				workbook.write(o);
	  				o.flush();
	  				o.close();
	  			 } else if(keyObj.equals("3")) {
	  				HSSFWorkbook workbook = new HSSFWorkbook();
	  	  		    HSSFSheet sheet = workbook.createSheet("其他简历汇总");
	  				othName =  dirNameRoot+"其他招聘简历汇总"+File.separator; 
	  				List<AggRMPsndocVO>  aggList = (List<AggRMPsndocVO>)PsnvoMap.get("3");
	  				createScoiExcel(sheet,workbook,aggList,map_org,map_dept,map_job,map_defdoc,map_region);
	  				File file = new File(othName);
	  				file.mkdirs();
	  			    BufferedWriter write = new BufferedWriter(new FileWriter(othName+"简历汇总.xls")); 
	  			    write.close();
	  				OutputStream o = new FileOutputStream(othName+"简历汇总.xls");			
	  				workbook.write(o);
	  				o.flush();
	  				o.close();
	  			 }	  			  
	  			  
	  		  } 					
				    	  
	    	  MessageDialog.showHintDlg(this.getEntranceUI(), "提示", "汇总简历导出成功！");
	      }
	}
	
	private void createScoiExcel(HSSFSheet sheet, HSSFWorkbook workbook, List<AggRMPsndocVO> selectDatas, 
			Map map_org,Map map_dept, Map map_job, Map map_defdoc,Map map_region){
		
		HSSFRow row_0 = sheet.createRow(0);
		row_0.setHeight((short)600);
		String titles[] = {"面试地点","部门","车间/室","报名岗位","是否服从调剂","人员编码","姓名","性别",
							"民族","出生年月","政治面貌","累计工龄","轨道工龄","净身高","体重","色盲/色弱",
							"婚育情况","籍贯","职称/技能","最高学历","全日制学历","全日制学位","全日制毕业院校",
							"全日制专业","毕业时间","现工作单位","所在部门","岗位","身份证号","联系方式","邮箱","备注"};
		for(int i=0;i<titles.length;i++){
			HSSFCell cell = row_0.createCell((short)i);
			cell.setCellValue(titles[i]);
			cell.setCellStyle(getHeadStyle(workbook));
		}
		for(int i=0;i<titles.length;i++){
			sheet.setColumnWidth(i,3000);	
		}
		
		
		for(int i=0;i<selectDatas.size();i++){
			//构建Excel表格内容
			List list_cell = new ArrayList<HSSFCell>();
    		AggRMPsndocVO aggvo = (AggRMPsndocVO)selectDatas.get(i);
    		RMPsndocVO psnvo = aggvo.getPsndocVO();//应聘登记人员信息VO
    		CircularlyAccessibleValueObject[] bvos = aggvo.getAllChildrenVO();
    		List<RMEduVO> eduvoList= new ArrayList();
    		List<RMPsnWorkVO> workvoList= new ArrayList();
    		
    		RMPsnJobVO  jobvo =null;//应聘职位
    		RMEduVO eduvo = null;//教育经历
    		RMPsnWorkVO workvo =null;//工作经历
    		String str = "无";//未填写提示
    		if(bvos.length>0){
    			for(int j=0;j<bvos.length;j++){
    				String classname = bvos[j].getClass().getName();
    				if("nc.vo.rm.psndoc.RMPsnJobVO".equals(classname)){
    					jobvo = (RMPsnJobVO) bvos[j];//应聘职位VO   					
    				}
    				if("nc.vo.rm.psndoc.RMEduVO".equals(classname)){
    					eduvoList.add((RMEduVO) bvos[j]);
    				}
    				if("nc.vo.rm.psndoc.RMPsnWorkVO".equals(classname)){    			
    					workvoList.add((RMPsnWorkVO)bvos[j]) ;
    				}
    			}
    		}
    		
    		HSSFRow row = sheet.createRow(i+1);
    		row.setHeight((short)500);
    		HSSFCell cell_0 = row.createCell((short)0);//序号
    		cell_0.setCellValue(psnvo.getAttributeValue("glbdef1")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef1")).toString());
    		list_cell.add(cell_0);
    		String dept=null;
    		if(map_dept.get(jobvo.getPk_reg_dept())!=null){
    			dept = map_dept.get(jobvo.getPk_reg_dept()).toString();
    		} 
    		int dex = dept.indexOf("-");
    		HSSFCell cell_1 = row.createCell((short)1);//部门
    		cell_1.setCellValue(dept==null?str:(dex==-1?dept:dept.substring(0,dex)));
    		list_cell.add(cell_1); 			
    		HSSFCell cell_2 = row.createCell((short)2);//车间/室
    		cell_2.setCellValue(dept==null?str:(dex==-1?str:dept.substring(dex+1)));
    		list_cell.add(cell_2);
    		HSSFCell cell_3 = row.createCell((short)3);//报名岗位
    		cell_3.setCellValue(map_job.get(jobvo.getPk_reg_job())==null?str:map_job.get(jobvo.getPk_reg_job()).toString());
    		list_cell.add(cell_3);
    		HSSFCell cell_4 = row.createCell((short)4);//是否服从调剂
    		cell_4.setCellValue(psnvo.getAttributeValue("glbdef25")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef25")).toString());
    		list_cell.add(cell_4);
    		HSSFCell cell_5 = row.createCell((short)5);//人员编码
    		cell_5.setCellValue(psnvo.getName()+psnvo.getId().substring(psnvo.getId().length()-6));
    		list_cell.add(cell_5);		
    		HSSFCell cell_6 = row.createCell((short)6);//姓名
    		cell_6.setCellValue(psnvo.getName()==null?str:psnvo.getName());
    		list_cell.add(cell_6);
    		HSSFCell cell_7 = row.createCell((short)7);//性别
    		cell_7.setCellValue(psnvo.getSex()==0?str:(psnvo.getSex()==1?"男":"女"));
    		list_cell.add(cell_7);
    		HSSFCell cell_8 = row.createCell((short)8);//民族
    		cell_8.setCellValue(psnvo.getNationality()==null?str:map_defdoc.get(psnvo.getNationality()).toString());
    		list_cell.add(cell_8);
    		HSSFCell cell_9 = row.createCell((short)9);//出生年月
    		cell_9.setCellValue(psnvo.getBirthdate().toString()==null?str:psnvo.getBirthdate().toString());
    		list_cell.add(cell_9);
    		HSSFCell cell_10 = row.createCell((short)10);//政治面貌
    		cell_10.setCellValue(psnvo.getPolity()==null?str:map_defdoc.get(psnvo.getPolity()).toString());
    		list_cell.add(cell_10);
    		HSSFCell cell_11 = row.createCell((short)11);//累计工龄
    		cell_11.setCellValue(psnvo.getAttributeValue("glbdef18")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef18")).toString());
    		list_cell.add(cell_11);
    		HSSFCell cell_12 = row.createCell((short)12);//轨道工龄
    		cell_12.setCellValue(psnvo.getAttributeValue("glbdef19")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef19")).toString());
    		list_cell.add(cell_12);
    		HSSFCell cell_13 = row.createCell((short)13);//净身高
    		cell_13.setCellValue(psnvo.getAttributeValue("glbdef2")==null?str:psnvo.getAttributeValue("glbdef2").toString());
    		list_cell.add(cell_13);
    		HSSFCell cell_14 = row.createCell((short)14);//体重
    		cell_14.setCellValue(psnvo.getAttributeValue("glbdef4")==null?str:psnvo.getAttributeValue("glbdef4").toString());
    		list_cell.add(cell_14);
    		HSSFCell cell_15 = row.createCell((short)15);//色盲/色弱
    		cell_15.setCellValue(psnvo.getAttributeValue("glbdef17")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef17")).toString());
    		list_cell.add(cell_15);
    		HSSFCell cell_16 = row.createCell((short)16);//婚育情况
    		cell_16.setCellValue(psnvo.getMarital()==null?str:map_defdoc.get(psnvo.getMarital()).toString());
    		list_cell.add(cell_16);
    		HSSFCell cell_17 = row.createCell((short)17);//籍贯
    		cell_17.setCellValue(psnvo.getNativeplace()==null?str:map_region.get(psnvo.getNativeplace()).toString());
    		list_cell.add(cell_17);
    		HSSFCell cell_18 = row.createCell((short)18);//职称/技能
    		cell_18.setCellValue(psnvo.getTitletechpost()==null?str:psnvo.getTitletechpost());
    		list_cell.add(cell_18);
    		HSSFCell cell_19 = row.createCell((short)19);//最高学历
    		cell_19.setCellValue(psnvo.getAttributeValue("glbdef28")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef28")).toString());
    		list_cell.add(cell_19);    		
    		HSSFCell cell_20 = row.createCell((short)20);//全日制学历
    		cell_20.setCellValue(psnvo.getEdu()==null?str:map_defdoc.get(psnvo.getEdu()).toString());
    		list_cell.add(cell_20);
    		HSSFCell cell_21 = row.createCell((short)21);//全日制学位
    		cell_21.setCellValue(psnvo.getPk_degree()==null?str:map_defdoc.get(psnvo.getPk_degree()).toString());
    		list_cell.add(cell_21);
    		HSSFCell cell_22 = row.createCell((short)22);//全日制毕业院校
    		cell_22.setCellValue(psnvo.getAttributeValue("glbdef13")==null?str:psnvo.getAttributeValue("glbdef13").toString());
    		list_cell.add(cell_22);
    		HSSFCell cell_23 = row.createCell((short)23);//全日制专业
    		cell_23.setCellValue(psnvo.getAttributeValue("glbdef14")==null?str:psnvo.getAttributeValue("glbdef14").toString());
    		list_cell.add(cell_23);
    		HSSFCell cell_24 = row.createCell((short)24);//毕业时间
    		cell_24.setCellValue(psnvo.getGraduationdate()==null?str:psnvo.getGraduationdate().toString());
    		list_cell.add(cell_24);
    		HSSFCell cell_25 = row.createCell((short)25);//现工作单位
    		cell_25.setCellValue(psnvo.getWorkunitnow()==null?str:psnvo.getWorkunitnow());
    		list_cell.add(cell_25);
    		HSSFCell cell_26 = row.createCell((short)26);//所在部门
    		cell_26.setCellValue(psnvo.getAttributeValue("glbdef23")==null?str:psnvo.getAttributeValue("glbdef23").toString());
    		list_cell.add(cell_26);
    		HSSFCell cell_27 = row.createCell((short)27);//岗位
    		cell_27.setCellValue(psnvo.getAttributeValue("glbdef24")==null?str:psnvo.getAttributeValue("glbdef24").toString());
    		list_cell.add(cell_27);
    		HSSFCell cell_28 = row.createCell((short)28);//身份证号
    		cell_28.setCellValue(psnvo.getId()==null?str:psnvo.getId());
    		list_cell.add(cell_28);
    		HSSFCell cell_29 = row.createCell((short)29);//联系方式
    		cell_29.setCellValue(psnvo.getMobile()==null?str:psnvo.getMobile());
    		list_cell.add(cell_29);
    		HSSFCell cell_30 = row.createCell((short)30);//邮箱
    		cell_30.setCellValue(psnvo.getEmail()==null?str:psnvo.getEmail());
    		list_cell.add(cell_30);
    		HSSFCell cell_31 = row.createCell((short)31);//备注
    		cell_31.setCellValue("");
    		list_cell.add(cell_31);
    		HSSFFont f = workbook.createFont();
    		for(int m=0;m<list_cell.size();m++){
    			((HSSFCell)list_cell.get(m)).setCellStyle(getBodyStyle(workbook,f));
    		}	
    	}
		
				
	}
	
	/***
	 * 
	 * 
	 * 初选通过建立汇总
	 * @param sheet
	 * @param workbook
	 * @param selectDatas
	 * @param map_org
	 * @param map_dept
	 * @param map_job
	 * @param map_defdoc
	 * @param map_region
	 */
	
	
	private void createSchlExcel(HSSFSheet sheet, HSSFWorkbook workbook, List<AggRMPsndocVO> selectDatas, 
			Map map_org,Map map_dept, Map map_job, Map map_defdoc,Map map_region){
		
		HSSFRow row_0 = sheet.createRow(0);
		row_0.setHeight((short)600);
		String titles[] = {"面试地点","简历编号","部门","车间/室","报名岗位","是否服从调剂","姓名","性别",
						   "民族","出生年月","籍贯","生源地","净身高（cm）","体重（kg）","政治面貌",
						   "裸眼视力（左/右）","色盲/色弱","健康状况","婚育情况","全日制学历","全日制学位",
						   "全日制毕业院校","全日制专业","全日制毕业时间","录取批次","学制","专业成绩排名","担任学校职务","英语等级/成绩","计算机水平",
						   "已考取证书","实习单位名称","担任职位","家庭住址","身份证号","联系电话","邮箱"};
		for(int i=0;i<titles.length;i++){
			 HSSFCell cell = row_0.createCell((short)i);
			 cell.setCellValue(titles[i]);
			 cell.setCellStyle(getHeadStyle(workbook));
		}
		for(int i=0;i<titles.length;i++){
			 sheet.setColumnWidth(i,3000);
		}
		
		
		for(int i=0;i<selectDatas.size();i++){
			//构建Excel表格内容
			List list_cell = new ArrayList<HSSFCell>();
    		AggRMPsndocVO aggvo = (AggRMPsndocVO)selectDatas.get(i);
    		RMPsndocVO psnvo = aggvo.getPsndocVO();//应聘登记人员信息VO
    		
    		CircularlyAccessibleValueObject[] bvos = aggvo.getAllChildrenVO();
    		RMPsnJobVO  jobvo =null;//应聘职位
    		RMEduVO eduvo = null;//教育经历
    		RMPsnWorkVO workvo =null;//工作经历
    		String str = "无";//未填写提示
    		if(bvos.length>0){
    			for(int j=0;j<bvos.length;j++){
    				String classname = bvos[j].getClass().getName();
    				if("nc.vo.rm.psndoc.RMPsnJobVO".equals(classname)){
    					jobvo = (RMPsnJobVO) bvos[j];//应聘职位VO   					
    				}
    				if("nc.vo.rm.psndoc.RMEduVO".equals(classname)){
    					eduvo =(RMEduVO) bvos[j];
    				}
    				if("nc.vo.rm.psndoc.RMPsnWorkVO".equals(classname)){    			
    					workvo = (RMPsnWorkVO) bvos[j];
    				}
    			}
    		}
    		    		
    		
    		HSSFRow row = sheet.createRow(i+1);
    		row.setHeight((short)500);
    		HSSFCell cell_0 = row.createCell((short)0);//序号
    		cell_0.setCellValue(psnvo.getAttributeValue("glbdef1")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef1")).toString());
    		list_cell.add(cell_0);   		
    		
    		HSSFCell cell_1 = row.createCell((short)1);//简历编号
    		cell_1.setCellValue(psnvo.getName()+psnvo.getId().substring(psnvo.getId().length()-6));
    		list_cell.add(cell_1); 			
    		HSSFCell cell_2 = row.createCell((short)2);//部门
    		String dept = null;
    		if(map_dept.get(jobvo.getPk_reg_dept())!=null){
    			dept =map_dept.get(jobvo.getPk_reg_dept()).toString();
    		}
            int dex = dept.indexOf("-");
    		cell_2.setCellValue(dept==null?str:(dex==-1?dept:dept.substring(0, dex)));
    		list_cell.add(cell_2);
    		HSSFCell cell_3 = row.createCell((short)3);//车间/室
    		cell_3.setCellValue(dept==null?str:(dex==-1?str:dept.substring(dex+1)));
    		list_cell.add(cell_3);
    		HSSFCell cell_4 = row.createCell((short)4);//报名岗位
    		cell_4.setCellValue(map_job.get(jobvo.getPk_reg_job())==null?str:map_job.get(jobvo.getPk_reg_job()).toString());
    		list_cell.add(cell_4);
    		HSSFCell cell_5 = row.createCell((short)5);//是否服从调剂
    		cell_5.setCellValue(psnvo.getAttributeValue("glbdef25")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef25")).toString());
    		list_cell.add(cell_5);		
   
    		HSSFCell cell_6 = row.createCell((short)6);//姓名
    		cell_6.setCellValue(psnvo.getName()==null?str:psnvo.getName());
    		list_cell.add(cell_6);
    		HSSFCell cell_7 = row.createCell((short)7);//性别
    		cell_7.setCellValue(psnvo.getSex()==0?str:(psnvo.getSex()==1?"男":"女"));
    		list_cell.add(cell_7);
    		HSSFCell cell_8 = row.createCell((short)8);//民族
    		cell_8.setCellValue(psnvo.getNationality()==null?str:map_defdoc.get(psnvo.getNationality()).toString());
    		list_cell.add(cell_8);
    		HSSFCell cell_9 = row.createCell((short)9);//出生年月
    		cell_9.setCellValue(psnvo.getBirthdate().toString()==null?str:psnvo.getBirthdate().toString());
    		list_cell.add(cell_9);
 		
    		HSSFCell cell_10 = row.createCell((short)10);//籍贯
    		cell_10.setCellValue(psnvo.getNativeplace()==null?str:map_region.get(psnvo.getNativeplace()).toString());
    		list_cell.add(cell_10);
    		
    		
    		
    		HSSFCell cell_11 = row.createCell((short)11);//生源地
    		cell_11.setCellValue(psnvo.getPermanreside()==null?str:map_region.get(psnvo.getPermanreside()).toString());
    		list_cell.add(cell_11);
    		HSSFCell cell_12 = row.createCell((short)12);//净身高
    		cell_12.setCellValue(psnvo.getAttributeValue("glbdef2")==null?str:psnvo.getAttributeValue("glbdef2").toString());
    		list_cell.add(cell_12);
    		HSSFCell cell_13 = row.createCell((short)13);//体重
    		cell_13.setCellValue(psnvo.getAttributeValue("glbdef4")==null?str:psnvo.getAttributeValue("glbdef4").toString());
    		list_cell.add(cell_13);
    		HSSFCell cell_14 = row.createCell((short)14);//政治面貌
    		cell_14.setCellValue(psnvo.getPolity()==null?str:map_defdoc.get(psnvo.getPolity()).toString());
    		list_cell.add(cell_14);
    		HSSFCell cell_15 = row.createCell((short)15);//裸眼视力
    		cell_15.setCellValue(psnvo.getAttributeValue("glbdef3")==null?str:psnvo.getAttributeValue("glbdef3").toString());
    		list_cell.add(cell_15);
    		HSSFCell cell_16 = row.createCell((short)16);//色盲/色弱
    		cell_16.setCellValue(psnvo.getAttributeValue("glbdef17")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef17")).toString());
    		list_cell.add(cell_16);
    		HSSFCell cell_17 = row.createCell((short)17);//健康状况
    		cell_17.setCellValue(psnvo.getHealth()==null?str:map_defdoc.get(psnvo.getHealth()).toString());
    		list_cell.add(cell_17);
    		HSSFCell cell_18 = row.createCell((short)18);//婚育状况
    		cell_18.setCellValue(psnvo.getMarital()==null?str:map_defdoc.get(psnvo.getMarital()).toString());
    		list_cell.add(cell_18);
    		HSSFCell cell_19 = row.createCell((short)19);//全日制学历
    		cell_19.setCellValue(psnvo.getEdu()==null?str:map_defdoc.get(psnvo.getEdu()).toString());
    		list_cell.add(cell_19);    		
    		HSSFCell cell_20 = row.createCell((short)20);//全日制学位
    		cell_20.setCellValue(psnvo.getPk_degree()==null?str:map_defdoc.get(psnvo.getPk_degree()).toString());
    		list_cell.add(cell_20);
    		HSSFCell cell_21 = row.createCell((short)21);//全日制毕业院校
    		cell_21.setCellValue(psnvo.getAttributeValue("glbdef13")==null?str:psnvo.getAttributeValue("glbdef13").toString());
    		list_cell.add(cell_21);
    		HSSFCell cell_22 = row.createCell((short)22);//全日制专业
    		cell_22.setCellValue(psnvo.getAttributeValue("glbdef14")==null?str:psnvo.getAttributeValue("glbdef14").toString());
    		list_cell.add(cell_22);
    		HSSFCell cell_23 = row.createCell((short)23);//全日制毕业时间
    		cell_23.setCellValue(psnvo.getGraduationdate()==null?str:psnvo.getGraduationdate().toString());
    		list_cell.add(cell_23);
    		
    		HSSFCell cell_24 = row.createCell((short)24);//录取批次
    		cell_24.setCellValue(psnvo.getAttributeValue("glbdef22")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef22")).toString());
    		list_cell.add(cell_24);
    		HSSFCell cell_25 = row.createCell((short)25);//学制
    		cell_25.setCellValue(psnvo.getAttributeValue("glbdef21")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef21")).toString());
    		list_cell.add(cell_25);
    		
    		HSSFCell cell_26 = row.createCell((short)26);//专业成绩排名
    		cell_26.setCellValue(psnvo.getAttributeValue("glbdef10")==null?str:psnvo.getAttributeValue("glbdef10").toString());
    		list_cell.add(cell_26);
    		HSSFCell cell_27 = row.createCell((short)27);//担任学校职务
    		cell_27.setCellValue(psnvo.getAttributeValue("glbdef9")==null?str:psnvo.getAttributeValue("glbdef9").toString());
    		list_cell.add(cell_27);
    		HSSFCell cell_28 = row.createCell((short)28);//外语水平
    		cell_28.setCellValue(psnvo.getFroeignlang()==null?str:psnvo.getFroeignlang());
    		list_cell.add(cell_28);
    		HSSFCell cell_29 = row.createCell((short)29);//计算机水平
    		cell_29.setCellValue(psnvo.getComputerlevel()==null?str:psnvo.getComputerlevel());
    		list_cell.add(cell_29);
    		HSSFCell cell_30 = row.createCell((short)30);//已考取证书
    		cell_30.setCellValue(psnvo.getAttributeValue("glbdef5")==null?str:psnvo.getAttributeValue("glbdef5").toString());
    		list_cell.add(cell_30);
    		HSSFCell cell_31 = row.createCell((short)31);//实习单位名称
    		cell_31.setCellValue(workvo==null?str:(workvo.getWorkcorp()==null?str:workvo.getWorkcorp()));
    		list_cell.add(cell_31);
    		HSSFCell cell_32 = row.createCell((short)32);//担任职位
    		cell_32.setCellValue(workvo==null?str:(workvo.getWorkjob()==null?str:workvo.getWorkjob()));
    		list_cell.add(cell_32);
    		HSSFCell cell_33 = row.createCell((short)33);//家庭住址
    		cell_33.setCellValue(psnvo.getAttributeValue("glbdef15")==null?str:psnvo.getAttributeValue("glbdef15").toString());
    		list_cell.add(cell_33);
    		HSSFCell cell_34 = row.createCell((short)34);//身份证号
    		cell_34.setCellValue(psnvo.getId()==null?str:psnvo.getId());
    		list_cell.add(cell_34);
    		HSSFCell cell_35 = row.createCell((short)35);//联系电话
    		cell_35.setCellValue(psnvo.getMobile()==null?str:psnvo.getMobile());
    		list_cell.add(cell_35);
    		HSSFCell cell_36 = row.createCell((short)36);//邮箱
    		cell_36.setCellValue(psnvo.getEmail()==null?str:psnvo.getEmail());
    		list_cell.add(cell_36);
    		HSSFFont f = workbook.createFont();
    		for(int m=0;m<list_cell.size();m++){
    			((HSSFCell)list_cell.get(m)).setCellStyle(getBodyStyle(workbook,f));
    		}	
    		
    	}
		
				
	}
	//安校园、社会招聘筛选人员
	public Map getPsnvoMap(Object[] selectDatas){
		//校园招聘key：1，社会招聘key:2,其他Key:3
		Map psnvoMap = new HashMap<String,List<AggRMPsndocVO>>();
		List schlList = new ArrayList<AggRMPsndocVO>();//校园集合
		List scoiList = new ArrayList<AggRMPsndocVO>();//社会集合
		List otherList = new ArrayList<AggRMPsndocVO>();//其他集合
		/**
		 * 集合标志位
		 */
		int schlIndex = 0;
		int scoiIndex = 0;
		int otherIndex = 0;
		for(Object obj:selectDatas){
			AggRMPsndocVO aggvo=(AggRMPsndocVO)obj;
			CircularlyAccessibleValueObject[] bvos = aggvo.getAllChildrenVO();
			if(bvos.length>0){							
				for(int i=0;i<bvos.length;i++){
					String classname = bvos[i].getClass().getName();
					if("nc.vo.rm.psndoc.RMPsnJobVO".equals(classname)){
						//RMPsndocVO psnvo = aggvo.getPsndocVO();//应聘登记人员信息VO
						RMPsnJobVO jobvo = (RMPsnJobVO) bvos[i];//应聘职位VO
						int sourcetype  = jobvo.getSourcetype();
						if(sourcetype==7){
							scoiIndex = 1;
							scoiList.add(aggvo);														
						}else if(sourcetype==11){
							schlIndex = 1;
							schlList.add(aggvo);														
						}else{
							otherIndex = 1;	
							otherList.add(aggvo);
						}
		            }
			     }
		      }
	      }
		if(schlIndex != 0){
			psnvoMap.put("1", schlList);	
		}
		if(scoiIndex != 0){
			psnvoMap.put("2", scoiList);	
		}
		if(otherIndex != 0	){
			psnvoMap.put("3", otherList);
		}
		
		return psnvoMap;
	}
	
	
	
	
		
	private HSSFCellStyle getHeadStyle(HSSFWorkbook workbook){
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// 下边框
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
		HSSFFont f = workbook.createFont();
		f.setFontName("宋体");
		f.setFontHeightInPoints((short)12);// 字号
		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(f);
		style.setWrapText(true);
		return style;
	}
	
	
	private HSSFCellStyle getBodyStyle(HSSFWorkbook workbook,HSSFFont f){
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// 下边框
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中		
		f.setFontName("宋体");
		f.setFontHeightInPoints((short)10);// 字号
		//f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(f);
		style.setWrapText(true);
		return style;
	}
	
	/**
	 * 设置单元格格式
	 * @param list_cell 
	 * @param workbook 
	 */
	private void setCellStyle(HSSFWorkbook workbook, List list_cell){
		HSSFCellStyle style_2 = workbook.createCellStyle();
		HSSFFont f_2 = workbook.createFont();
		style_2.setAlignment(HSSFCellStyle.ALIGN_CENTER);//
		style_2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
		f_2.setFontHeightInPoints((short) 10.5);// 字号
		style_2.setFont(f_2);
		style_2.setBorderBottom((short) 1);
		style_2.setBorderLeft((short) 1);
		style_2.setBorderRight((short) 1);
		style_2.setBorderTop((short) 1);
		for(int i=0;i<list_cell.size();i++){
			((HSSFCell)list_cell.get(i)).setCellStyle(style_2);
		}
	}
}
