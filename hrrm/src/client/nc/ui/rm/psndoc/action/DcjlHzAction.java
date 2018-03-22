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
 * �������ܼ�����
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
		  String name = "�������ܼ���";
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
		  Object object =MessageDialog.showSelectDlg(this.getEntranceUI(),"�̷�ѡ��", "��ѡ����������̷�(Ĭ��C��)��", objs,roots.length);
		  if(object==null){
			  return;		  		  
		  }	
		  String panfu  = object.toString();
		  Object[] selectDatas = ((BillManageModel)getModel()).getSelectedOperaDatas();
	      if (ArrayUtils.isEmpty(selectDatas)) {
	        throw new BusinessException("��ѡ����Ա��!");
	      }else{
	    	  IUAPQueryBS bs = NCLocator.getInstance().lookup(IUAPQueryBS.class);
	    	  String sql_org = "select code,name,pk_adminorg from org_adminorg where nvl(dr,0)=0";
	    	  List list_org = (List) bs.executeQuery(sql_org.toString(), new ArrayListProcessor());
	    	  Map map_org = new HashMap();//ӦƸ��֯
	    	  if(list_org.size()>0){
	    		  for(int i=0;i<list_org.size();i++){
	    			  Object[] obj = (Object[]) list_org.get(i);
	    			  map_org.put(obj[2].toString(),obj[1].toString());
	    		  }
	    	  }
	    	  String sql_dept = "select code,name,pk_dept from org_dept where nvl(dr,0)=0";
	    	  List list_dept = (List) bs.executeQuery(sql_dept, new ArrayListProcessor());
	    	  Map map_dept = new HashMap();//ӦƸ����
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
	    	  Map map_job = new HashMap();//ӦƸְλ
	    	  if(list_job.size()>0){
	    		  for(int i=0;i<list_job.size();i++){
	    			  Object[] obj = (Object[]) list_job.get(i);
	    			  map_job.put(obj[2].toString(),obj[1].toString());
	    		  }
	    	  }
	    	  String sql_defdoc = "select name,pk_defdoc from bd_defdoc where nvl(dr,0)=0";
	    	  List list_defdoc = (List) bs.executeQuery(sql_defdoc, new ArrayListProcessor());
	    	  Map map_defdoc = new HashMap();//�Զ��嵵��
	    	  for(int i=0;i<list_defdoc.size();i++){
	    		  Object[] obj = (Object[]) list_defdoc.get(i);
	    		  map_defdoc.put(obj[1].toString(), obj[0].toString());
	    	  }	 	    	  
	    	  String sql_bdregion = "select name,pk_region from bd_region where nvl(dr,0)=0";
	    	  List list_region= (List)bs.executeQuery(sql_bdregion, new ArrayListProcessor());
	    	  Map map_region = new HashMap();//��������
	    	  for(int i=0;i<list_region.size();i++){
	    		  Object[] obj = (Object[]) list_region.get(i);
	    		  map_region.put(obj[1].toString(), obj[0].toString());
	    	  }	    	  
	  		  String date = (new UFDate()).getYear()+"-"+(new UFDate()).getMonth()+"-"+(new UFDate()).getDay();
			  String dirNameRoot = panfu+"�������ܵ���"+ File.separator;
			  String schlName ="";
			  String scoiName =""; 
			  String othName ="";
	  		  if(status==1){
	  			  dirNameRoot += "ӦƸ�Ǽ���Ա��������"+ File.separator+date+File.separator;				   
			  }else if(status==2){
				  dirNameRoot += "��ѡͨ����Ա��������"+ File.separator+date+File.separator;			   
			  }
	  		  Map  PsnvoMap= getPsnvoMap(selectDatas);
	  		  Set keySet=PsnvoMap.keySet();
	  		  Iterator itt = keySet.iterator();
	  		  while(itt.hasNext()){	 
	  			  Object keyObj=(String)itt.next();
	  			 if(keyObj.equals("1")){
	  				HSSFWorkbook workbook = new HSSFWorkbook();
	  	  		    HSSFSheet sheet = workbook.createSheet("У԰��������");
	  				schlName =  dirNameRoot+"У԰��Ƹ��������"+File.separator;
	  				List<AggRMPsndocVO>  aggList = (List<AggRMPsndocVO>)PsnvoMap.get("1");
	  				createSchlExcel(sheet,workbook,aggList,map_org,map_dept,map_job,map_defdoc,map_region);
	  				File file = new File(schlName);
	  				file.mkdirs();
	  			    BufferedWriter write = new BufferedWriter(new FileWriter(schlName+"��������.xls")); 
	  			    write.close();
	  				OutputStream o = new FileOutputStream(schlName+"��������.xls");			
	  				workbook.write(o);
	  				o.flush();
	  				o.close();
	  			 }else if(keyObj.equals("2")){
	  				HSSFWorkbook workbook = new HSSFWorkbook();
	  	  		    HSSFSheet sheet = workbook.createSheet("����������");
	  				scoiName =  dirNameRoot+"�����Ƹ��������"+File.separator;
	  				List<AggRMPsndocVO>  aggList = (List<AggRMPsndocVO>)PsnvoMap.get("2");
	  				//createSchlExcel(sheet,workbook,aggList,map_org,map_dept,map_job,map_defdoc,map_region);
	  				createScoiExcel(sheet,workbook,aggList,map_org,map_dept,map_job,map_defdoc,map_region);
	  				File file = new File(scoiName);
	  				file.mkdirs();
	  			    BufferedWriter write = new BufferedWriter(new FileWriter(scoiName+"��������.xls")); 
	  			    write.close();
	  				OutputStream o = new FileOutputStream(scoiName+"��������.xls");			
	  				workbook.write(o);
	  				o.flush();
	  				o.close();
	  			 } else if(keyObj.equals("3")) {
	  				HSSFWorkbook workbook = new HSSFWorkbook();
	  	  		    HSSFSheet sheet = workbook.createSheet("������������");
	  				othName =  dirNameRoot+"������Ƹ��������"+File.separator; 
	  				List<AggRMPsndocVO>  aggList = (List<AggRMPsndocVO>)PsnvoMap.get("3");
	  				createScoiExcel(sheet,workbook,aggList,map_org,map_dept,map_job,map_defdoc,map_region);
	  				File file = new File(othName);
	  				file.mkdirs();
	  			    BufferedWriter write = new BufferedWriter(new FileWriter(othName+"��������.xls")); 
	  			    write.close();
	  				OutputStream o = new FileOutputStream(othName+"��������.xls");			
	  				workbook.write(o);
	  				o.flush();
	  				o.close();
	  			 }	  			  
	  			  
	  		  } 					
				    	  
	    	  MessageDialog.showHintDlg(this.getEntranceUI(), "��ʾ", "���ܼ��������ɹ���");
	      }
	}
	
	private void createScoiExcel(HSSFSheet sheet, HSSFWorkbook workbook, List<AggRMPsndocVO> selectDatas, 
			Map map_org,Map map_dept, Map map_job, Map map_defdoc,Map map_region){
		
		HSSFRow row_0 = sheet.createRow(0);
		row_0.setHeight((short)600);
		String titles[] = {"���Եص�","����","����/��","������λ","�Ƿ���ӵ���","��Ա����","����","�Ա�",
							"����","��������","������ò","�ۼƹ���","�������","�����","����","ɫä/ɫ��",
							"�������","����","ְ��/����","���ѧ��","ȫ����ѧ��","ȫ����ѧλ","ȫ���Ʊ�ҵԺУ",
							"ȫ����רҵ","��ҵʱ��","�ֹ�����λ","���ڲ���","��λ","���֤��","��ϵ��ʽ","����","��ע"};
		for(int i=0;i<titles.length;i++){
			HSSFCell cell = row_0.createCell((short)i);
			cell.setCellValue(titles[i]);
			cell.setCellStyle(getHeadStyle(workbook));
		}
		for(int i=0;i<titles.length;i++){
			sheet.setColumnWidth(i,3000);	
		}
		
		
		for(int i=0;i<selectDatas.size();i++){
			//����Excel�������
			List list_cell = new ArrayList<HSSFCell>();
    		AggRMPsndocVO aggvo = (AggRMPsndocVO)selectDatas.get(i);
    		RMPsndocVO psnvo = aggvo.getPsndocVO();//ӦƸ�Ǽ���Ա��ϢVO
    		CircularlyAccessibleValueObject[] bvos = aggvo.getAllChildrenVO();
    		List<RMEduVO> eduvoList= new ArrayList();
    		List<RMPsnWorkVO> workvoList= new ArrayList();
    		
    		RMPsnJobVO  jobvo =null;//ӦƸְλ
    		RMEduVO eduvo = null;//��������
    		RMPsnWorkVO workvo =null;//��������
    		String str = "��";//δ��д��ʾ
    		if(bvos.length>0){
    			for(int j=0;j<bvos.length;j++){
    				String classname = bvos[j].getClass().getName();
    				if("nc.vo.rm.psndoc.RMPsnJobVO".equals(classname)){
    					jobvo = (RMPsnJobVO) bvos[j];//ӦƸְλVO   					
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
    		HSSFCell cell_0 = row.createCell((short)0);//���
    		cell_0.setCellValue(psnvo.getAttributeValue("glbdef1")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef1")).toString());
    		list_cell.add(cell_0);
    		String dept=null;
    		if(map_dept.get(jobvo.getPk_reg_dept())!=null){
    			dept = map_dept.get(jobvo.getPk_reg_dept()).toString();
    		} 
    		int dex = dept.indexOf("-");
    		HSSFCell cell_1 = row.createCell((short)1);//����
    		cell_1.setCellValue(dept==null?str:(dex==-1?dept:dept.substring(0,dex)));
    		list_cell.add(cell_1); 			
    		HSSFCell cell_2 = row.createCell((short)2);//����/��
    		cell_2.setCellValue(dept==null?str:(dex==-1?str:dept.substring(dex+1)));
    		list_cell.add(cell_2);
    		HSSFCell cell_3 = row.createCell((short)3);//������λ
    		cell_3.setCellValue(map_job.get(jobvo.getPk_reg_job())==null?str:map_job.get(jobvo.getPk_reg_job()).toString());
    		list_cell.add(cell_3);
    		HSSFCell cell_4 = row.createCell((short)4);//�Ƿ���ӵ���
    		cell_4.setCellValue(psnvo.getAttributeValue("glbdef25")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef25")).toString());
    		list_cell.add(cell_4);
    		HSSFCell cell_5 = row.createCell((short)5);//��Ա����
    		cell_5.setCellValue(psnvo.getName()+psnvo.getId().substring(psnvo.getId().length()-6));
    		list_cell.add(cell_5);		
    		HSSFCell cell_6 = row.createCell((short)6);//����
    		cell_6.setCellValue(psnvo.getName()==null?str:psnvo.getName());
    		list_cell.add(cell_6);
    		HSSFCell cell_7 = row.createCell((short)7);//�Ա�
    		cell_7.setCellValue(psnvo.getSex()==0?str:(psnvo.getSex()==1?"��":"Ů"));
    		list_cell.add(cell_7);
    		HSSFCell cell_8 = row.createCell((short)8);//����
    		cell_8.setCellValue(psnvo.getNationality()==null?str:map_defdoc.get(psnvo.getNationality()).toString());
    		list_cell.add(cell_8);
    		HSSFCell cell_9 = row.createCell((short)9);//��������
    		cell_9.setCellValue(psnvo.getBirthdate().toString()==null?str:psnvo.getBirthdate().toString());
    		list_cell.add(cell_9);
    		HSSFCell cell_10 = row.createCell((short)10);//������ò
    		cell_10.setCellValue(psnvo.getPolity()==null?str:map_defdoc.get(psnvo.getPolity()).toString());
    		list_cell.add(cell_10);
    		HSSFCell cell_11 = row.createCell((short)11);//�ۼƹ���
    		cell_11.setCellValue(psnvo.getAttributeValue("glbdef18")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef18")).toString());
    		list_cell.add(cell_11);
    		HSSFCell cell_12 = row.createCell((short)12);//�������
    		cell_12.setCellValue(psnvo.getAttributeValue("glbdef19")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef19")).toString());
    		list_cell.add(cell_12);
    		HSSFCell cell_13 = row.createCell((short)13);//�����
    		cell_13.setCellValue(psnvo.getAttributeValue("glbdef2")==null?str:psnvo.getAttributeValue("glbdef2").toString());
    		list_cell.add(cell_13);
    		HSSFCell cell_14 = row.createCell((short)14);//����
    		cell_14.setCellValue(psnvo.getAttributeValue("glbdef4")==null?str:psnvo.getAttributeValue("glbdef4").toString());
    		list_cell.add(cell_14);
    		HSSFCell cell_15 = row.createCell((short)15);//ɫä/ɫ��
    		cell_15.setCellValue(psnvo.getAttributeValue("glbdef17")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef17")).toString());
    		list_cell.add(cell_15);
    		HSSFCell cell_16 = row.createCell((short)16);//�������
    		cell_16.setCellValue(psnvo.getMarital()==null?str:map_defdoc.get(psnvo.getMarital()).toString());
    		list_cell.add(cell_16);
    		HSSFCell cell_17 = row.createCell((short)17);//����
    		cell_17.setCellValue(psnvo.getNativeplace()==null?str:map_region.get(psnvo.getNativeplace()).toString());
    		list_cell.add(cell_17);
    		HSSFCell cell_18 = row.createCell((short)18);//ְ��/����
    		cell_18.setCellValue(psnvo.getTitletechpost()==null?str:psnvo.getTitletechpost());
    		list_cell.add(cell_18);
    		HSSFCell cell_19 = row.createCell((short)19);//���ѧ��
    		cell_19.setCellValue(psnvo.getAttributeValue("glbdef28")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef28")).toString());
    		list_cell.add(cell_19);    		
    		HSSFCell cell_20 = row.createCell((short)20);//ȫ����ѧ��
    		cell_20.setCellValue(psnvo.getEdu()==null?str:map_defdoc.get(psnvo.getEdu()).toString());
    		list_cell.add(cell_20);
    		HSSFCell cell_21 = row.createCell((short)21);//ȫ����ѧλ
    		cell_21.setCellValue(psnvo.getPk_degree()==null?str:map_defdoc.get(psnvo.getPk_degree()).toString());
    		list_cell.add(cell_21);
    		HSSFCell cell_22 = row.createCell((short)22);//ȫ���Ʊ�ҵԺУ
    		cell_22.setCellValue(psnvo.getAttributeValue("glbdef13")==null?str:psnvo.getAttributeValue("glbdef13").toString());
    		list_cell.add(cell_22);
    		HSSFCell cell_23 = row.createCell((short)23);//ȫ����רҵ
    		cell_23.setCellValue(psnvo.getAttributeValue("glbdef14")==null?str:psnvo.getAttributeValue("glbdef14").toString());
    		list_cell.add(cell_23);
    		HSSFCell cell_24 = row.createCell((short)24);//��ҵʱ��
    		cell_24.setCellValue(psnvo.getGraduationdate()==null?str:psnvo.getGraduationdate().toString());
    		list_cell.add(cell_24);
    		HSSFCell cell_25 = row.createCell((short)25);//�ֹ�����λ
    		cell_25.setCellValue(psnvo.getWorkunitnow()==null?str:psnvo.getWorkunitnow());
    		list_cell.add(cell_25);
    		HSSFCell cell_26 = row.createCell((short)26);//���ڲ���
    		cell_26.setCellValue(psnvo.getAttributeValue("glbdef23")==null?str:psnvo.getAttributeValue("glbdef23").toString());
    		list_cell.add(cell_26);
    		HSSFCell cell_27 = row.createCell((short)27);//��λ
    		cell_27.setCellValue(psnvo.getAttributeValue("glbdef24")==null?str:psnvo.getAttributeValue("glbdef24").toString());
    		list_cell.add(cell_27);
    		HSSFCell cell_28 = row.createCell((short)28);//���֤��
    		cell_28.setCellValue(psnvo.getId()==null?str:psnvo.getId());
    		list_cell.add(cell_28);
    		HSSFCell cell_29 = row.createCell((short)29);//��ϵ��ʽ
    		cell_29.setCellValue(psnvo.getMobile()==null?str:psnvo.getMobile());
    		list_cell.add(cell_29);
    		HSSFCell cell_30 = row.createCell((short)30);//����
    		cell_30.setCellValue(psnvo.getEmail()==null?str:psnvo.getEmail());
    		list_cell.add(cell_30);
    		HSSFCell cell_31 = row.createCell((short)31);//��ע
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
	 * ��ѡͨ����������
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
		String titles[] = {"���Եص�","�������","����","����/��","������λ","�Ƿ���ӵ���","����","�Ա�",
						   "����","��������","����","��Դ��","����ߣ�cm��","���أ�kg��","������ò",
						   "������������/�ң�","ɫä/ɫ��","����״��","�������","ȫ����ѧ��","ȫ����ѧλ",
						   "ȫ���Ʊ�ҵԺУ","ȫ����רҵ","ȫ���Ʊ�ҵʱ��","¼ȡ����","ѧ��","רҵ�ɼ�����","����ѧУְ��","Ӣ��ȼ�/�ɼ�","�����ˮƽ",
						   "�ѿ�ȡ֤��","ʵϰ��λ����","����ְλ","��ͥסַ","���֤��","��ϵ�绰","����"};
		for(int i=0;i<titles.length;i++){
			 HSSFCell cell = row_0.createCell((short)i);
			 cell.setCellValue(titles[i]);
			 cell.setCellStyle(getHeadStyle(workbook));
		}
		for(int i=0;i<titles.length;i++){
			 sheet.setColumnWidth(i,3000);
		}
		
		
		for(int i=0;i<selectDatas.size();i++){
			//����Excel�������
			List list_cell = new ArrayList<HSSFCell>();
    		AggRMPsndocVO aggvo = (AggRMPsndocVO)selectDatas.get(i);
    		RMPsndocVO psnvo = aggvo.getPsndocVO();//ӦƸ�Ǽ���Ա��ϢVO
    		
    		CircularlyAccessibleValueObject[] bvos = aggvo.getAllChildrenVO();
    		RMPsnJobVO  jobvo =null;//ӦƸְλ
    		RMEduVO eduvo = null;//��������
    		RMPsnWorkVO workvo =null;//��������
    		String str = "��";//δ��д��ʾ
    		if(bvos.length>0){
    			for(int j=0;j<bvos.length;j++){
    				String classname = bvos[j].getClass().getName();
    				if("nc.vo.rm.psndoc.RMPsnJobVO".equals(classname)){
    					jobvo = (RMPsnJobVO) bvos[j];//ӦƸְλVO   					
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
    		HSSFCell cell_0 = row.createCell((short)0);//���
    		cell_0.setCellValue(psnvo.getAttributeValue("glbdef1")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef1")).toString());
    		list_cell.add(cell_0);   		
    		
    		HSSFCell cell_1 = row.createCell((short)1);//�������
    		cell_1.setCellValue(psnvo.getName()+psnvo.getId().substring(psnvo.getId().length()-6));
    		list_cell.add(cell_1); 			
    		HSSFCell cell_2 = row.createCell((short)2);//����
    		String dept = null;
    		if(map_dept.get(jobvo.getPk_reg_dept())!=null){
    			dept =map_dept.get(jobvo.getPk_reg_dept()).toString();
    		}
            int dex = dept.indexOf("-");
    		cell_2.setCellValue(dept==null?str:(dex==-1?dept:dept.substring(0, dex)));
    		list_cell.add(cell_2);
    		HSSFCell cell_3 = row.createCell((short)3);//����/��
    		cell_3.setCellValue(dept==null?str:(dex==-1?str:dept.substring(dex+1)));
    		list_cell.add(cell_3);
    		HSSFCell cell_4 = row.createCell((short)4);//������λ
    		cell_4.setCellValue(map_job.get(jobvo.getPk_reg_job())==null?str:map_job.get(jobvo.getPk_reg_job()).toString());
    		list_cell.add(cell_4);
    		HSSFCell cell_5 = row.createCell((short)5);//�Ƿ���ӵ���
    		cell_5.setCellValue(psnvo.getAttributeValue("glbdef25")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef25")).toString());
    		list_cell.add(cell_5);		
   
    		HSSFCell cell_6 = row.createCell((short)6);//����
    		cell_6.setCellValue(psnvo.getName()==null?str:psnvo.getName());
    		list_cell.add(cell_6);
    		HSSFCell cell_7 = row.createCell((short)7);//�Ա�
    		cell_7.setCellValue(psnvo.getSex()==0?str:(psnvo.getSex()==1?"��":"Ů"));
    		list_cell.add(cell_7);
    		HSSFCell cell_8 = row.createCell((short)8);//����
    		cell_8.setCellValue(psnvo.getNationality()==null?str:map_defdoc.get(psnvo.getNationality()).toString());
    		list_cell.add(cell_8);
    		HSSFCell cell_9 = row.createCell((short)9);//��������
    		cell_9.setCellValue(psnvo.getBirthdate().toString()==null?str:psnvo.getBirthdate().toString());
    		list_cell.add(cell_9);
 		
    		HSSFCell cell_10 = row.createCell((short)10);//����
    		cell_10.setCellValue(psnvo.getNativeplace()==null?str:map_region.get(psnvo.getNativeplace()).toString());
    		list_cell.add(cell_10);
    		
    		
    		
    		HSSFCell cell_11 = row.createCell((short)11);//��Դ��
    		cell_11.setCellValue(psnvo.getPermanreside()==null?str:map_region.get(psnvo.getPermanreside()).toString());
    		list_cell.add(cell_11);
    		HSSFCell cell_12 = row.createCell((short)12);//�����
    		cell_12.setCellValue(psnvo.getAttributeValue("glbdef2")==null?str:psnvo.getAttributeValue("glbdef2").toString());
    		list_cell.add(cell_12);
    		HSSFCell cell_13 = row.createCell((short)13);//����
    		cell_13.setCellValue(psnvo.getAttributeValue("glbdef4")==null?str:psnvo.getAttributeValue("glbdef4").toString());
    		list_cell.add(cell_13);
    		HSSFCell cell_14 = row.createCell((short)14);//������ò
    		cell_14.setCellValue(psnvo.getPolity()==null?str:map_defdoc.get(psnvo.getPolity()).toString());
    		list_cell.add(cell_14);
    		HSSFCell cell_15 = row.createCell((short)15);//��������
    		cell_15.setCellValue(psnvo.getAttributeValue("glbdef3")==null?str:psnvo.getAttributeValue("glbdef3").toString());
    		list_cell.add(cell_15);
    		HSSFCell cell_16 = row.createCell((short)16);//ɫä/ɫ��
    		cell_16.setCellValue(psnvo.getAttributeValue("glbdef17")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef17")).toString());
    		list_cell.add(cell_16);
    		HSSFCell cell_17 = row.createCell((short)17);//����״��
    		cell_17.setCellValue(psnvo.getHealth()==null?str:map_defdoc.get(psnvo.getHealth()).toString());
    		list_cell.add(cell_17);
    		HSSFCell cell_18 = row.createCell((short)18);//����״��
    		cell_18.setCellValue(psnvo.getMarital()==null?str:map_defdoc.get(psnvo.getMarital()).toString());
    		list_cell.add(cell_18);
    		HSSFCell cell_19 = row.createCell((short)19);//ȫ����ѧ��
    		cell_19.setCellValue(psnvo.getEdu()==null?str:map_defdoc.get(psnvo.getEdu()).toString());
    		list_cell.add(cell_19);    		
    		HSSFCell cell_20 = row.createCell((short)20);//ȫ����ѧλ
    		cell_20.setCellValue(psnvo.getPk_degree()==null?str:map_defdoc.get(psnvo.getPk_degree()).toString());
    		list_cell.add(cell_20);
    		HSSFCell cell_21 = row.createCell((short)21);//ȫ���Ʊ�ҵԺУ
    		cell_21.setCellValue(psnvo.getAttributeValue("glbdef13")==null?str:psnvo.getAttributeValue("glbdef13").toString());
    		list_cell.add(cell_21);
    		HSSFCell cell_22 = row.createCell((short)22);//ȫ����רҵ
    		cell_22.setCellValue(psnvo.getAttributeValue("glbdef14")==null?str:psnvo.getAttributeValue("glbdef14").toString());
    		list_cell.add(cell_22);
    		HSSFCell cell_23 = row.createCell((short)23);//ȫ���Ʊ�ҵʱ��
    		cell_23.setCellValue(psnvo.getGraduationdate()==null?str:psnvo.getGraduationdate().toString());
    		list_cell.add(cell_23);
    		
    		HSSFCell cell_24 = row.createCell((short)24);//¼ȡ����
    		cell_24.setCellValue(psnvo.getAttributeValue("glbdef22")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef22")).toString());
    		list_cell.add(cell_24);
    		HSSFCell cell_25 = row.createCell((short)25);//ѧ��
    		cell_25.setCellValue(psnvo.getAttributeValue("glbdef21")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef21")).toString());
    		list_cell.add(cell_25);
    		
    		HSSFCell cell_26 = row.createCell((short)26);//רҵ�ɼ�����
    		cell_26.setCellValue(psnvo.getAttributeValue("glbdef10")==null?str:psnvo.getAttributeValue("glbdef10").toString());
    		list_cell.add(cell_26);
    		HSSFCell cell_27 = row.createCell((short)27);//����ѧУְ��
    		cell_27.setCellValue(psnvo.getAttributeValue("glbdef9")==null?str:psnvo.getAttributeValue("glbdef9").toString());
    		list_cell.add(cell_27);
    		HSSFCell cell_28 = row.createCell((short)28);//����ˮƽ
    		cell_28.setCellValue(psnvo.getFroeignlang()==null?str:psnvo.getFroeignlang());
    		list_cell.add(cell_28);
    		HSSFCell cell_29 = row.createCell((short)29);//�����ˮƽ
    		cell_29.setCellValue(psnvo.getComputerlevel()==null?str:psnvo.getComputerlevel());
    		list_cell.add(cell_29);
    		HSSFCell cell_30 = row.createCell((short)30);//�ѿ�ȡ֤��
    		cell_30.setCellValue(psnvo.getAttributeValue("glbdef5")==null?str:psnvo.getAttributeValue("glbdef5").toString());
    		list_cell.add(cell_30);
    		HSSFCell cell_31 = row.createCell((short)31);//ʵϰ��λ����
    		cell_31.setCellValue(workvo==null?str:(workvo.getWorkcorp()==null?str:workvo.getWorkcorp()));
    		list_cell.add(cell_31);
    		HSSFCell cell_32 = row.createCell((short)32);//����ְλ
    		cell_32.setCellValue(workvo==null?str:(workvo.getWorkjob()==null?str:workvo.getWorkjob()));
    		list_cell.add(cell_32);
    		HSSFCell cell_33 = row.createCell((short)33);//��ͥסַ
    		cell_33.setCellValue(psnvo.getAttributeValue("glbdef15")==null?str:psnvo.getAttributeValue("glbdef15").toString());
    		list_cell.add(cell_33);
    		HSSFCell cell_34 = row.createCell((short)34);//���֤��
    		cell_34.setCellValue(psnvo.getId()==null?str:psnvo.getId());
    		list_cell.add(cell_34);
    		HSSFCell cell_35 = row.createCell((short)35);//��ϵ�绰
    		cell_35.setCellValue(psnvo.getMobile()==null?str:psnvo.getMobile());
    		list_cell.add(cell_35);
    		HSSFCell cell_36 = row.createCell((short)36);//����
    		cell_36.setCellValue(psnvo.getEmail()==null?str:psnvo.getEmail());
    		list_cell.add(cell_36);
    		HSSFFont f = workbook.createFont();
    		for(int m=0;m<list_cell.size();m++){
    			((HSSFCell)list_cell.get(m)).setCellStyle(getBodyStyle(workbook,f));
    		}	
    		
    	}
		
				
	}
	//��У԰�������Ƹɸѡ��Ա
	public Map getPsnvoMap(Object[] selectDatas){
		//У԰��Ƹkey��1�������Ƹkey:2,����Key:3
		Map psnvoMap = new HashMap<String,List<AggRMPsndocVO>>();
		List schlList = new ArrayList<AggRMPsndocVO>();//У԰����
		List scoiList = new ArrayList<AggRMPsndocVO>();//��Ἧ��
		List otherList = new ArrayList<AggRMPsndocVO>();//��������
		/**
		 * ���ϱ�־λ
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
						//RMPsndocVO psnvo = aggvo.getPsndocVO();//ӦƸ�Ǽ���Ա��ϢVO
						RMPsnJobVO jobvo = (RMPsnJobVO) bvos[i];//ӦƸְλVO
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
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// �±߿�
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// ��߿�
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// �ұ߿�
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// �ϱ߿�
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// ���Ҿ���
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// ���¾���
		HSSFFont f = workbook.createFont();
		f.setFontName("����");
		f.setFontHeightInPoints((short)12);// �ֺ�
		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(f);
		style.setWrapText(true);
		return style;
	}
	
	
	private HSSFCellStyle getBodyStyle(HSSFWorkbook workbook,HSSFFont f){
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// �±߿�
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// ��߿�
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// �ұ߿�
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// �ϱ߿�
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// ���Ҿ���
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// ���¾���		
		f.setFontName("����");
		f.setFontHeightInPoints((short)10);// �ֺ�
		//f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(f);
		style.setWrapText(true);
		return style;
	}
	
	/**
	 * ���õ�Ԫ���ʽ
	 * @param list_cell 
	 * @param workbook 
	 */
	private void setCellStyle(HSSFWorkbook workbook, List list_cell){
		HSSFCellStyle style_2 = workbook.createCellStyle();
		HSSFFont f_2 = workbook.createFont();
		style_2.setAlignment(HSSFCellStyle.ALIGN_CENTER);//
		style_2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// ���¾���
		f_2.setFontHeightInPoints((short) 10.5);// �ֺ�
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
