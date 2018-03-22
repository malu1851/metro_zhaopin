package nc.ui.rm.psndoc.action;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import jxl.read.biff.Record;
import nc.bs.framework.common.NCLocator;
import nc.itf.uap.IUAPQueryBS;
import nc.pub.tools.VOUtils;
import nc.ui.pub.beans.MessageDialog;
import nc.vo.bd.address.AddressVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.lang.UFDate;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMEduVO;
import nc.vo.rm.psndoc.RMEncVO;
import nc.vo.rm.psndoc.RMFamilyVO;
import nc.vo.rm.psndoc.RMProjectVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.RMPsnWorkVO;
import nc.vo.rm.psndoc.RMPsndocVO;
import nc.vo.rm.psndoc.RMTrainVO;

import org.apache.activemq.protobuf.BufferInputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.hssf.util.Region;
//import org.hibernate.Hibernate;
import org.springframework.jdbc.core.JdbcTemplate;

/***
 * 
 * 
 * @author ������
 * ��������
 *
 */
public class DcjlExcelTool {

	@SuppressWarnings({ "unused", "rawtypes" })
	public void newExcel(AggRMPsndocVO aggvo, Map map_org, Map map_dept, Map map_job, Map map_defdoc,int status,String panfu,Map map_region) throws Exception {
		CircularlyAccessibleValueObject[] bvos = aggvo.getAllChildrenVO();
		if(bvos.length>0){
			String date = (new UFDate()).getYear()+"-"+(new UFDate()).getMonth()+"-"+(new UFDate()).getDay();
			String dirName = panfu +"��������"+ File.separator;
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMPsnJobVO".equals(classname)){
					RMPsndocVO psnvo = aggvo.getPsndocVO();//ӦƸ�Ǽ���Ա��ϢVO
					RMPsnJobVO jobvo = (RMPsnJobVO) bvos[i];//ӦƸְλVO
					String psnname = psnvo.getName();//ӦƸ��Ա����
					String idcode = psnvo.getId();//���֤��
					String org = (String) map_org.get(jobvo.getPk_reg_org());//ӦƸ��֯
					String dept = (String) map_dept.get(jobvo.getPk_reg_dept());//ӦƸ����
					String job = (String) map_job.get(jobvo.getPk_reg_job());//ӦƸְλ				
							
					if(status==1){
					   dirName += "ӦƸ�Ǽ���Ա"+ File.separator;
						
					}else if(status==2){
						dirName += "��ѡͨ����Ա"+ File.separator;				
					}	
								
					HSSFWorkbook workbook = new HSSFWorkbook();
					HSSFSheet sheet = workbook.createSheet(psnname+"����");										 				
					int sourcetype  = jobvo.getSourcetype();
					if(sourcetype==7){
						dirName+="�����Ƹ"+File.separator+date+File.separator+ 
								org+File.separator+dept+File.separator+job+File.separator;
						createSciExcel(sheet,workbook,aggvo,org,dept,job,map_defdoc,map_region);
					}else if(sourcetype==11){ 				    	
  				    	dirName+="У԰��Ƹ"+File.separator+date+File.separator+ 
								org+File.separator+dept+File.separator+job+File.separator;  				    	
  				    	createSchlExcel(sheet,workbook,aggvo,org,dept,job,map_defdoc,map_region);
  				    	 	
  				    }else{
  				    	dirName+="������ʽ��Ƹ"+File.separator+date+File.separator+ 
								org+File.separator+dept+File.separator+job+File.separator;				    	
  				    	createSciExcel(sheet,workbook,aggvo,org,dept,job,map_defdoc,map_region);		
  				    }	
										
					File file = new File(dirName);
					file.mkdirs();
					BufferedWriter write = new BufferedWriter(new FileWriter(dirName+psnname+idcode.substring(idcode.length()-6)+".xls")); 
			        write.close();
					OutputStream o = new FileOutputStream(dirName+psnname+idcode.substring(idcode.length()-6)+".xls");
					workbook.write(o);
					o.flush();
					o.close();
				}
			}
		}else{
			throw new Exception("ӦƸְλ��ϢΪ�գ������ƣ�");
		}
	}

	/**
	 * ���ñ�ͷ����ʽ
	 * 
	 * @param workbook
	 * @param cell_1
	 */
	private  HSSFCellStyle getHeadStyle(HSSFWorkbook workbook) {
		HSSFCellStyle style = workbook.createCellStyle();
		HSSFFont f = workbook.createFont();
		f.setFontName("����");
		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// �Ӵ�
		f.setFontHeightInPoints((short)14);// �ֺ�	
		style.setFont(f);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// �±߿�
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// ��߿�
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// �ұ߿�
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// �ϱ߿�
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// ���Ҿ���
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// ���¾���
		style.setWrapText(true);
		return style;
	}
	/**
	 * ���ñ�ͷ���ڵ���ʽ
	 * 
	 * @param workbook
	 * @param cell_1
	 */
	private  HSSFCellStyle getHeadDateStyle(HSSFWorkbook workbook) {
		HSSFCellStyle style = workbook.createCellStyle();
		HSSFFont f = workbook.createFont();
		f.setFontName("����");
		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// �Ӵ�
		f.setFontHeightInPoints((short)12);// �ֺ�	
		style.setFont(f);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// �±߿�
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// ��߿�
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// �ұ߿�
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// �ϱ߿�
		style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);// ƫ��
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// ���¾���
		return style;
	}
	/**
	 * ���ñ����б�ͷ����ʽ
	 * 
	 * @param workbook
	 * @param cell_1
	 */
		
	private  HSSFCellStyle getBodyHeadStyle(HSSFWorkbook workbook){
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// �±߿�
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// ��߿�
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// �ұ߿�
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// �ϱ߿�
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// ���Ҿ���
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// ���¾���
		HSSFFont f = workbook.createFont();
		f.setFontName("����");
		f.setFontHeightInPoints((short)10);// �ֺ�
		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(f);
		style.setWrapText(true);
		return style;
	}
	/**
	 * ���ñ������ʽ
	 * 
	 * @param workbook
	 * @param cell_1
	 */
	private  HSSFCellStyle getBodyStyle(HSSFWorkbook workbook){
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// �±߿�
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// ��߿�
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// �ұ߿�
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// �ϱ߿�
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// ���Ҿ���
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// ���¾���
		HSSFFont f = workbook.createFont();
		f.setFontName("����");
		f.setFontHeightInPoints((short)9);// �ֺ�
		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(f);
		style.setWrapText(true);
		return style;
	}
	
	/**
	 * ���ñ����ֶ�д�����ʽ
	 * 
	 * @param workbook
	 * @param cell_1
	 */
	private  HSSFCellStyle getBodyByPeopleStyle(HSSFWorkbook workbook){
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// �±߿�
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// ��߿�
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// �ұ߿�
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// �ϱ߿�
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// ���Ҿ���
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// ���¾���
		HSSFFont f = workbook.createFont();
		f.setFontName("����");
		f.setFontHeightInPoints((short)9);// �ֺ�
		//f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(f);
		style.setWrapText(true);
		return style;
	}
	/**
	 * ���ñ����������ۡ�ѧϰ���������ž�������ʽ
	 * 
	 * @param workbook
	 * @param cell_1
	 */
	private  HSSFCellStyle getBodyByJLStyle(HSSFWorkbook workbook){
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// �±߿�
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// ��߿�
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// �ұ߿�
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// �ϱ߿�
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);// ���Ҿ���
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// ���¾���
		HSSFFont f = workbook.createFont();
		f.setFontName("����");
		f.setFontHeightInPoints((short)9);// �ֺ�
		//f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(f);
		style.setWrapText(true);
		return style;
	}




	/**
	 * �������Excel
	 * 
	 * @param sheet
	 * @param workbook
	 * @param aggvo 
	 * @param map_defdoc 
	 * @param job 
	 * @param dept 
	 * @param org 
	 */
	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	private  void createSciExcel(HSSFSheet sheet, HSSFWorkbook workbook, AggRMPsndocVO aggvo, 
			String org, String dept, String job, Map map_defdoc,Map map_region) {		
		
		HSSFCellStyle headStyle =getHeadStyle(workbook);
		HSSFCellStyle headDateStyle =getHeadDateStyle(workbook);
		HSSFCellStyle bodyHeadStyle =getBodyHeadStyle(workbook);
		HSSFCellStyle bodyStyle =getBodyStyle(workbook);	
		
		/***
		 * �����п�
		 */
		sheet.setColumnWidth(0,2500);//A
		sheet.setColumnWidth(1,2500);
		sheet.setColumnWidth(2,800);//C
		sheet.setColumnWidth(3,1600);
		sheet.setColumnWidth(4,1600);//E
		sheet.setColumnWidth(5,2000);
		sheet.setColumnWidth(6,2000);//G
		sheet.setColumnWidth(7,2000);
		sheet.setColumnWidth(8,2000);//I
		sheet.setColumnWidth(9,2000);
		sheet.setColumnWidth(10,2500);//k	
		sheet.setColumnWidth(11,2500);	
		
		sheet.setMargin(HSSFSheet.TopMargin, (double)0.5);
		sheet.setMargin(HSSFSheet.BottomMargin,(double)0.5);
		sheet.setMargin(HSSFSheet.LeftMargin, (double)0.4);
		sheet.setMargin(HSSFSheet.RightMargin,(double)0.4);
		
		sheet.setHorizontallyCenter(true);
		sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
		
		
		String str = "��";//�ֶο�ʱ��ʾ�ַ�
		
		RMPsndocVO psnvo = aggvo.getPsndocVO();//ӦƸ�Ǽ���Ա��ϢVO		
		List list_cell = new ArrayList<HSSFCell>();//���嵥Ԫ��
		List list_peopleCell = new ArrayList<HSSFCell>();//�����ֶ�д�뵥Ԫ��
		List list_bodyHeadCell = new ArrayList<HSSFCell>();//�����ͷ��Ԫ��
		List list_row = new ArrayList<HSSFCell>();//��			
		
		/****����****/
		HSSFRow row_0 = sheet.createRow(0);
		row_0.setHeight((short)700);
		HSSFCell cell_name = row_0.createCell((short)0);
		cell_name.setCellValue(org+"ӦƸ��Ϣ��(�����Ƹ)");		
		cell_name.setCellStyle(headStyle);
		setRegionStyle(sheet,new Region(0,(short)0,0,(short)11),headStyle);
		sheet.addMergedRegion(new Region(0, (short)0,0, (short)11));
		
		/*********************************������Ϣbegin*******************************/
		/***�������***/
		HSSFRow row_1 = sheet.createRow(1);
		row_1.setHeight((short)400);
		HSSFCell celldate = row_1.createCell((short)0);
		celldate.setCellValue("������ڣ�"+(new UFDate()).getYear()+" ��  "+(new UFDate()).getMonth()+" �� "+(new UFDate()).getDay()+" ��");		
		celldate.setCellStyle(headDateStyle);
		setRegionStyle(sheet,new Region(1,(short)0,1,(short)11),headDateStyle);
		sheet.addMergedRegion(new Region((short)1, (short)0,(short)1, (short)11));
		
		
		/***ӦƸ��λ***/
		HSSFRow row_2 = sheet.createRow(2);
		list_row.add(row_2);
		HSSFCell cell_2_0 = row_2.createCell((short)0);
		list_cell.add(cell_2_0);
		cell_2_0.setCellValue("ӦƸ��λ");
		sheet.addMergedRegion(new Region((short)2, (short)0,(short)3, (short)0));
		HSSFCell cell_2_1 = row_2.createCell((short)1);
		list_cell.add(cell_2_1);
		cell_2_1.setCellValue("����");
		sheet.addMergedRegion(new Region((short)2, (short)1,(short)2, (short)2));
		HSSFCell cell_2_3 = row_2.createCell((short)3);
		list_cell.add(cell_2_3);
		cell_2_3.setCellValue("����/��");
		sheet.addMergedRegion(new Region((short)2, (short)3,(short)2, (short)4));
		HSSFCell cell_2_5 = row_2.createCell((short)5);
		list_cell.add(cell_2_5);
		cell_2_5.setCellValue("��λ");
		sheet.addMergedRegion(new Region((short)2, (short)5,(short)2, (short)6));
		HSSFCell cell_2_7 = row_2.createCell((short)7);
		list_cell.add(cell_2_7);
		cell_2_7.setCellValue("�Ƿ���Ӹ�λ����");
		sheet.addMergedRegion(new Region((short)2, (short)7,(short)3, (short)8));	
		HSSFCell cell_2_9 = row_2.createCell((short)9);
		list_peopleCell.add(cell_2_9);	
		cell_2_9.setCellValue(psnvo.getAttributeValue("glbdef25")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef25")).toString());
		sheet.addMergedRegion(new Region((short)2, (short)9,(short)3, (short)9));		
		
		Object photo = psnvo.getPhoto();
		byte[] pngBytes = null;
		if(photo==null){	
			HSSFCell cell_2_10 = row_2.createCell((short)8);
			list_cell.add(cell_2_10);
			cell_2_10.setCellValue("����Ƭ");//��Ƭ
			sheet.addMergedRegion(new Region(2, (short)10,6, (short)11));			
		}else{			
			photo = VOUtils.transPreviewPhoto((byte[]) photo);
			pngBytes = (byte[]) photo;
			HSSFPatriarch patriarch = sheet.createDrawingPatriarch(); 
			insertImage(workbook, patriarch, pngBytes, 2, 10, 7, 12,0);		
	    }
					
		int dex = dept.indexOf("-");
		HSSFRow row_3 = sheet.createRow(3);
		list_row.add(row_3);
		HSSFCell cell_3_1 = row_3.createCell((short)1);
		list_peopleCell.add(cell_3_1);
		cell_3_1.setCellValue(dept==null?str:(dex==-1?dept:dept.substring(0, dex)));//����
		sheet.addMergedRegion(new Region((short)3, (short)1,(short)3, (short)2));
		HSSFCell cell_3_3 = row_3.createCell((short)3);
		list_peopleCell.add(cell_3_3);
		cell_3_3.setCellValue(dept==null?str:(dex==-1?str:dept.substring(dex+1)));//����
		sheet.addMergedRegion(new Region((short)3, (short)3,(short)3, (short)4));
		HSSFCell cell_3_5 = row_3.createCell((short)5);
		list_peopleCell.add(cell_3_5);
		cell_3_5.setCellValue(job==null?str:job);//��λ
		sheet.addMergedRegion(new Region((short)3, (short)5,(short)3, (short)6));
		  
        
		HSSFRow row_4 = sheet.createRow(4);
		list_row.add(row_4);
		HSSFCell cell_4_0 = row_4.createCell((short)0);
		list_cell.add(cell_4_0);
		cell_4_0.setCellValue("����");//
		sheet.addMergedRegion(new Region((short)4, (short)0,(short)4, (short)0));
		HSSFCell cell_4_1 = row_4.createCell((short)1);
		list_peopleCell.add(cell_4_1);
		cell_4_1.setCellValue(psnvo.getName()==null?str:psnvo.getName());//����
		sheet.addMergedRegion(new Region((short)4, (short)1,(short)4, (short)2));
		HSSFCell cell_4_3 = row_4.createCell((short)3);
		list_cell.add(cell_4_3);
		cell_4_3.setCellValue("�Ա�");//�Ա�
		sheet.addMergedRegion(new Region((short)4, (short)3,(short)4, (short)4));
		HSSFCell cell_4_5 = row_4.createCell((short)5);
		list_peopleCell.add(cell_4_5);
		cell_4_5.setCellValue(psnvo.getSex()==0?str:(psnvo.getSex()==1?"��":"Ů"));//�Ա�
		sheet.addMergedRegion(new Region((short)4, (short)5,(short)4, (short)6));
		HSSFCell cell_4_7 = row_4.createCell((short)7);
		list_cell.add(cell_4_7);
		cell_4_7.setCellValue("����");
		sheet.addMergedRegion(new Region((short)4, (short)7,(short)4, (short)8));
		HSSFCell cell_4_9 = row_4.createCell((short)9);
		list_peopleCell.add(cell_4_9);
		cell_4_9.setCellValue(psnvo.getNationality()==null?str:map_defdoc.get(psnvo.getNationality()).toString());//���� 
		sheet.addMergedRegion(new Region((short)4, (short)9,(short)4, (short)9));
		
		HSSFRow row_5 = sheet.createRow(5);
		list_row.add(row_5);
		HSSFCell cell_5_0 = row_5.createCell((short)0);
		list_cell.add(cell_5_0);
		cell_5_0.setCellValue("��������");//
		sheet.addMergedRegion(new Region((short)5, (short)0,(short)5, (short)0));
		HSSFCell cell_5_1 = row_5.createCell((short)1);
		list_peopleCell.add(cell_5_1);
		cell_5_1.setCellValue(psnvo.getBirthdate().toString()==null?str:psnvo.getBirthdate().toString());//
		sheet.addMergedRegion(new Region((short)5, (short)1,(short)5, (short)2));
		HSSFCell cell_5_3 = row_5.createCell((short)3);
		list_cell.add(cell_5_3);
		cell_5_3.setCellValue("������ò");//
		sheet.addMergedRegion(new Region((short)5, (short)3,(short)5, (short)4));
		HSSFCell cell_5_5 = row_5.createCell((short)5);
		list_peopleCell.add(cell_5_5);
		cell_5_5.setCellValue(psnvo.getPolity()==null?str:map_defdoc.get(psnvo.getPolity()).toString());//
		sheet.addMergedRegion(new Region((short)5, (short)5,(short)5, (short)6));
		HSSFCell cell_5_7 = row_5.createCell((short)7);
		list_cell.add(cell_5_7);
		cell_5_7.setCellValue("����״��");
		sheet.addMergedRegion(new Region((short)5, (short)7,(short)5, (short)8));
		HSSFCell cell_5_9 = row_5.createCell((short)9);
		list_peopleCell.add(cell_5_9);
		cell_5_9.setCellValue(psnvo.getHealth()==null?str:map_defdoc.get(psnvo.getHealth()).toString());//���� 
		sheet.addMergedRegion(new Region((short)5, (short)9,(short)5, (short)9));
		
		
		HSSFRow row_6 = sheet.createRow(6);
		list_row.add(row_6);
		HSSFCell cell_6_0 = row_6.createCell((short)0);
		list_cell.add(cell_6_0);
		cell_6_0.setCellValue("�����(cm)");//
		sheet.addMergedRegion(new Region((short)6, (short)0,(short)6, (short)0));
		HSSFCell cell_6_1 = row_6.createCell((short)1);
		list_peopleCell.add(cell_6_1);
		cell_6_1.setCellValue(psnvo.getAttributeValue("glbdef2")==null?str:psnvo.getAttributeValue("glbdef2").toString());//
		sheet.addMergedRegion(new Region((short)6, (short)1,(short)6, (short)2));
		HSSFCell cell_6_3 = row_6.createCell((short)3);
		list_cell.add(cell_6_3);
		cell_6_3.setCellValue("����(KG)");//
		sheet.addMergedRegion(new Region((short)6, (short)3,(short)6, (short)4));
		HSSFCell cell_6_5 = row_6.createCell((short)5);
		list_peopleCell.add(cell_6_5);
		cell_6_5.setCellValue(psnvo.getAttributeValue("glbdef4")==null?str:psnvo.getAttributeValue("glbdef4").toString());//
		sheet.addMergedRegion(new Region((short)6, (short)5,(short)6, (short)6));
		HSSFCell cell_6_7 = row_6.createCell((short)7);
		list_cell.add(cell_6_7);
		cell_6_7.setCellValue("����״��");
		sheet.addMergedRegion(new Region((short)6, (short)7,(short)6, (short)8));
		HSSFCell cell_6_9 = row_6.createCell((short)9);
		list_peopleCell.add(cell_6_9);
		cell_6_9.setCellValue(psnvo.getMarital()==null?str:map_defdoc.get(psnvo.getMarital()).toString());// 
		sheet.addMergedRegion(new Region((short)6, (short)9,(short)6, (short)9));
		
		
		HSSFRow row_7 = sheet.createRow(7);
		list_row.add(row_7);
		HSSFCell cell_7_0 = row_7.createCell((short)0);
		list_cell.add(cell_7_0);
		cell_7_0.setCellValue("��������(��/��)");//
		sheet.addMergedRegion(new Region((short)7, (short)0,(short)7, (short)0));
		HSSFCell cell_7_1 = row_7.createCell((short)1);
		list_peopleCell.add(cell_7_1);
		cell_7_1.setCellValue(psnvo.getAttributeValue("glbdef3")==null?str:psnvo.getAttributeValue("glbdef3").toString());//
		sheet.addMergedRegion(new Region((short)7, (short)1,(short)7, (short)2));
		HSSFCell cell_7_3 = row_7.createCell((short)3);
		list_cell.add(cell_7_3);
		cell_7_3.setCellValue("ɫä/ɫ��");//
		sheet.addMergedRegion(new Region((short)7, (short)3,(short)7, (short)4));
		HSSFCell cell_7_5 = row_7.createCell((short)5);
		list_peopleCell.add(cell_7_5);
		cell_7_5.setCellValue(psnvo.getAttributeValue("glbdef17")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef17")).toString());//
		sheet.addMergedRegion(new Region((short)7, (short)5,(short)7, (short)6));
		HSSFCell cell_7_7 = row_7.createCell((short)7);
		list_cell.add(cell_7_7);
		cell_7_7.setCellValue("���֤��");
		sheet.addMergedRegion(new Region((short)7, (short)7,(short)7, (short)8));
		HSSFCell cell_7_9 = row_7.createCell((short)9);
		list_peopleCell.add(cell_7_9);
		cell_7_9.setCellValue(psnvo.getId()==null?str:psnvo.getId());// 
		sheet.addMergedRegion(new Region((short)7, (short)9,(short)7, (short)11));
		
		HSSFRow row_8 = sheet.createRow(8);
		list_row.add(row_8);
		HSSFCell cell_8_0 = row_8.createCell((short)0);
		list_cell.add(cell_8_0);
		cell_8_0.setCellValue("ְ��/����");//
		sheet.addMergedRegion(new Region((short)8, (short)0,(short)8, (short)0));
		HSSFCell cell_8_1 = row_8.createCell((short)1);
		list_peopleCell.add(cell_8_1);	
		
		cell_8_1.setCellValue(psnvo.getTitletechpost()==null?str:psnvo.getTitletechpost());//
		sheet.addMergedRegion(new Region((short)8, (short)1,(short)8, (short)2));
		HSSFCell cell_8_3 = row_8.createCell((short)3);
		list_cell.add(cell_8_3);
		cell_8_3.setCellValue("��ϵ�绰");//
		sheet.addMergedRegion(new Region((short)8, (short)3,(short)8, (short)4));
		HSSFCell cell_8_5 = row_8.createCell((short)5);
		list_peopleCell.add(cell_8_5);
		cell_8_5.setCellValue(psnvo.getMobile()==null?str:psnvo.getMobile());//
		sheet.addMergedRegion(new Region((short)8, (short)5,(short)8, (short)6));
		HSSFCell cell_8_7 = row_8.createCell((short)7);
		list_cell.add(cell_8_7);
		cell_8_7.setCellValue("��������");
		sheet.addMergedRegion(new Region((short)8, (short)7,(short)8, (short)8));
		HSSFCell cell_8_9 = row_8.createCell((short)9);
		list_peopleCell.add(cell_8_9);
		cell_8_9.setCellValue(psnvo.getEmail()==null?str:psnvo.getEmail());// 
		sheet.addMergedRegion(new Region((short)8, (short)9,(short)8, (short)11));
		
		
		HSSFRow row_9 = sheet.createRow(9);
		list_row.add(row_9);
		HSSFCell cell_9_0 = row_9.createCell((short)0);
		list_cell.add(cell_9_0);
		cell_9_0.setCellValue("ȫ����ѧ��");//
		sheet.addMergedRegion(new Region((short)9, (short)0,(short)9, (short)0));
		HSSFCell cell_9_1 = row_9.createCell((short)1);
		list_peopleCell.add(cell_9_1);
		cell_9_1.setCellValue(psnvo.getEdu()==null?str:map_defdoc.get(psnvo.getEdu()).toString());//
		sheet.addMergedRegion(new Region((short)9, (short)1,(short)9, (short)2));
		HSSFCell cell_9_3 = row_9.createCell((short)3);
		list_cell.add(cell_9_3);
		cell_9_3.setCellValue("ȫ���Ʊ�ҵԺУ");//
		sheet.addMergedRegion(new Region((short)9, (short)3,(short)9, (short)4));
		HSSFCell cell_9_5 = row_9.createCell((short)5);
		list_peopleCell.add(cell_9_5);
		cell_9_5.setCellValue(psnvo.getAttributeValue("glbdef13")==null?str:psnvo.getAttributeValue("glbdef13").toString());//
		sheet.addMergedRegion(new Region((short)9, (short)5,(short)9, (short)6));
		HSSFCell cell_9_7 = row_9.createCell((short)7);
		list_cell.add(cell_9_7);
		cell_9_7.setCellValue("�к��س�");
		sheet.addMergedRegion(new Region((short)9, (short)7,(short)9, (short)8));
		HSSFCell cell_9_9 = row_9.createCell((short)9);
		list_peopleCell.add(cell_9_9);
		cell_9_9.setCellValue(psnvo.getPersonal()==null?str:psnvo.getPersonal());// 
		sheet.addMergedRegion(new Region((short)9, (short)9,(short)9, (short)11));
		
		HSSFRow row_10 = sheet.createRow(10);
		list_row.add(row_10);
		HSSFCell cell_10_0 = row_10.createCell((short)0);
		list_cell.add(cell_10_0);
		cell_10_0.setCellValue("ȫ����ѧλ");//
		sheet.addMergedRegion(new Region((short)10, (short)0,(short)10, (short)0));
		HSSFCell cell_10_1 = row_10.createCell((short)1);
		list_peopleCell.add(cell_10_1);
		cell_10_1.setCellValue(psnvo.getPk_degree()==null?str:map_defdoc.get(psnvo.getPk_degree()).toString());//
		sheet.addMergedRegion(new Region((short)10, (short)1,(short)10, (short)2));
		HSSFCell cell_10_3 = row_10.createCell((short)3);
		list_cell.add(cell_10_3);
		cell_10_3.setCellValue("ȫ����רҵ");//
		sheet.addMergedRegion(new Region((short)10, (short)3,(short)10, (short)4));
		HSSFCell cell_10_5 = row_10.createCell((short)5);
		list_peopleCell.add(cell_10_5);
		cell_10_5.setCellValue(psnvo.getAttributeValue("glbdef14")==null?str:psnvo.getAttributeValue("glbdef14").toString());//
		sheet.addMergedRegion(new Region((short)10, (short)5,(short)10, (short)6));
		HSSFCell cell_10_7 = row_10.createCell((short)7);
		list_cell.add(cell_10_7);
		cell_10_7.setCellValue("������н(��)");
		sheet.addMergedRegion(new Region((short)10, (short)7,(short)10, (short)8));
		HSSFCell cell_10_9 = row_10.createCell((short)9);
		list_peopleCell.add(cell_10_9);
		cell_10_9.setCellValue(psnvo.getExpect_wage()==null?str:psnvo.getExpect_wage().toString());// 
		sheet.addMergedRegion(new Region((short)10, (short)9,(short)10, (short)11));
		
		HSSFRow row_11 = sheet.createRow(11);
		list_row.add(row_11);
		HSSFCell cell_11_0 = row_11.createCell((short)0);
		list_cell.add(cell_11_0);
		cell_11_0.setCellValue("���ѧ��");//
		sheet.addMergedRegion(new Region((short)11, (short)0,(short)11, (short)0));
		HSSFCell cell_11_1 = row_11.createCell((short)1);
		list_peopleCell.add(cell_11_1);
		cell_11_1.setCellValue(psnvo.getAttributeValue("glbdef28")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef28")).toString());//
		sheet.addMergedRegion(new Region((short)11, (short)1,(short)11, (short)2));
		HSSFCell cell_11_3 = row_11.createCell((short)3);
		list_cell.add(cell_11_3);
		cell_11_3.setCellValue("����");//
		sheet.addMergedRegion(new Region((short)11, (short)3,(short)11, (short)4));
		HSSFCell cell_11_5 = row_11.createCell((short)5);
		list_peopleCell.add(cell_11_5);
		cell_11_5.setCellValue(psnvo.getNativeplace()==null?str:map_region.get(psnvo.getNativeplace()).toString());//
		sheet.addMergedRegion(new Region((short)11, (short)5,(short)11, (short)6));
		HSSFCell cell_11_7 = row_11.createCell((short)7);
		list_cell.add(cell_11_7);
		cell_11_7.setCellValue("��ͥ���ڵ�");
		sheet.addMergedRegion(new Region((short)11, (short)7,(short)11, (short)8));
		HSSFCell cell_11_9 = row_11.createCell((short)9);
		list_peopleCell.add(cell_11_9);
		//cell_11_9.setCellValue(psnvo.getStudentposition()==null?str:psnvo.getStudentposition());// 
		cell_11_9.setCellValue(psnvo.getAttributeValue("glbdef15")==null?str:psnvo.getAttributeValue("glbdef15").toString());// 
		sheet.addMergedRegion(new Region((short)11, (short)9,(short)11, (short)11));
		
		
		HSSFRow row_12 = sheet.createRow(12);
		list_row.add(row_12);
		HSSFCell cell_12_0 = row_12.createCell((short)0);
		list_cell.add(cell_12_0);
		cell_12_0.setCellValue("�ۼƹ���");//
		sheet.addMergedRegion(new Region((short)12, (short)0,(short)12, (short)0));
		HSSFCell cell_12_1 = row_12.createCell((short)1);
		list_peopleCell.add(cell_12_1);
		cell_12_1.setCellValue(psnvo.getAttributeValue("glbdef18")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef18")).toString());//
		sheet.addMergedRegion(new Region((short)12, (short)1,(short)12, (short)2));
		HSSFCell cell_12_3 = row_12.createCell((short)3);
		list_cell.add(cell_12_3);
		cell_12_3.setCellValue("�����ͨ����");//
		sheet.addMergedRegion(new Region((short)12, (short)3,(short)12, (short)4));
		HSSFCell cell_12_5 = row_12.createCell((short)5);
		list_peopleCell.add(cell_12_5);
		cell_12_5.setCellValue(psnvo.getAttributeValue("glbdef19")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef19")).toString());//
		sheet.addMergedRegion(new Region((short)12, (short)5,(short)12, (short)6));
		HSSFCell cell_12_7 = row_12.createCell((short)7);
		list_cell.add(cell_12_7);
		cell_12_7.setCellValue("ӦƸ��˾ԭ��");
		sheet.addMergedRegion(new Region((short)12, (short)7,(short)12, (short)8));
		HSSFCell cell_12_9 = row_12.createCell((short)9);
		list_peopleCell.add(cell_12_9);
		cell_12_9.setCellValue(psnvo.getAttributeValue("glbdef8")==null?str:psnvo.getAttributeValue("glbdef8").toString());// 
		sheet.addMergedRegion(new Region((short)12, (short)9,(short)12, (short)11));
		/*********************************������Ϣend*******************************/
		
		/*********************************��������begin*******************************/
		HSSFRow row_13 = sheet.createRow(13);
		list_row.add(row_13);
		HSSFCell cell_13_0 = row_13.createCell((short)0);
		list_bodyHeadCell.add(cell_13_0);
		cell_13_0.setCellValue("�� �� �� ���������ѧ����ʼ��д����д�����л����ר��");//
		sheet.addMergedRegion(new Region((short)13, (short)0,(short)13, (short)11));
		HSSFRow row_14 = sheet.createRow(14);
		list_row.add(row_14);
		HSSFCell cell_14_0 = row_14.createCell((short)0);
		list_cell.add(cell_14_0);
		cell_14_0.setCellValue("��ʼʱ��");//
		sheet.addMergedRegion(new Region((short)14, (short)0,(short)14, (short)0));
		HSSFCell cell_14_1 = row_14.createCell((short)1);
		list_cell.add(cell_14_1);
		cell_14_1.setCellValue("��ֹʱ��");//
		sheet.addMergedRegion(new Region((short)14, (short)1,(short)14, (short)1));
		HSSFCell cell_14_2 = row_14.createCell((short)2);
		list_cell.add(cell_14_2);
		cell_14_2.setCellValue("ѧУ����");//
		sheet.addMergedRegion(new Region((short)14, (short)2,(short)14, (short)4));
		HSSFCell cell_14_5 = row_14.createCell((short)5);
		list_cell.add(cell_14_5);
		cell_14_5.setCellValue("ѧϰ����");//
		sheet.addMergedRegion(new Region((short)14, (short)5,(short)14, (short)5));
		HSSFCell cell_14_6 = row_14.createCell((short)6);
		list_cell.add(cell_14_6);
		cell_14_6.setCellValue("�Ƿ�ȫ����");//
		sheet.addMergedRegion(new Region((short)14, (short)6,(short)14, (short)6));
		HSSFCell cell_14_7 = row_14.createCell((short)7);
		list_cell.add(cell_14_7);
		cell_14_7.setCellValue("רҵ");//
		sheet.addMergedRegion(new Region((short)14, (short)7,(short)14, (short)8));
		HSSFCell cell_14_9 = row_14.createCell((short)9);
		list_cell.add(cell_14_9);
		cell_14_9.setCellValue("ѧ��");//
		sheet.addMergedRegion(new Region((short)14, (short)9,(short)14, (short)9));
		HSSFCell cell_14_10 = row_14.createCell((short)10);
		list_cell.add(cell_14_10);
		cell_14_10.setCellValue("ѧλ");//
		sheet.addMergedRegion(new Region((short)14, (short)10,(short)14, (short)11));
		CircularlyAccessibleValueObject[] bvos = aggvo.getAllChildrenVO();
		bvos = getBvosBySort(bvos);		
		int index=0;
		if(bvos.length>0){
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMEduVO".equals(classname)){
					index=index+1;
					RMEduVO eduvo = (RMEduVO) bvos[i];
					HSSFRow row_edu = sheet.createRow(14+index);
					list_row.add(row_edu);
					HSSFCell cell_edu_0 = row_edu.createCell((short)0);
					list_peopleCell.add(cell_edu_0);
					cell_edu_0.setCellValue(eduvo.getBegindate()==null?str:eduvo.getBegindate()+"");//��ʼʱ��
					sheet.addMergedRegion(new Region((short)(14+index), (short)0,(short)(14+index), (short)0));
					HSSFCell cell_edu_1 = row_edu.createCell((short)1);
					list_peopleCell.add(cell_edu_1);
					cell_edu_1.setCellValue(eduvo.getEnddate()==null?str:eduvo.getEnddate()+"");//����ʱ��
					sheet.addMergedRegion(new Region((short)(14+index), (short)1,(short)(14+index), (short)1));
					HSSFCell cell_edu_2 = row_edu.createCell((short)2);
					list_peopleCell.add(cell_edu_2);
					cell_edu_2.setCellValue(eduvo.getSchool()==null?str:eduvo.getSchool());//ѧУ
					sheet.addMergedRegion(new Region((short)(14+index), (short)2,(short)(14+index), (short)4));
					HSSFCell cell_edu_5 = row_edu.createCell((short)5);
					list_peopleCell.add(cell_edu_5);
					int xxnx = 0;
					if(eduvo.getBegindate() != null && !"".equals(eduvo.getBegindate()) 
							&& eduvo.getEnddate() != null && !"".equals(eduvo.getEnddate())){
						xxnx = eduvo.getBegindate().getYear() - eduvo.getEnddate().getYear();
					}
					cell_edu_5.setCellValue(eduvo.getEdusystem()==null?str:eduvo.getEdusystem().toString());//ѧϰ����
					sheet.addMergedRegion(new Region((short)(14+index), (short)5,(short)(14+index), (short)5));
					HSSFCell cell_edu_6 = row_edu.createCell((short)6);
					list_peopleCell.add(cell_edu_6);
					cell_edu_6.setCellValue(eduvo.getAttributeValue("glbdef4").toString()=="N"?"��":"��");//�Ƿ�ȫ����
					sheet.addMergedRegion(new Region((short)(14+index), (short)6,(short)(14+index), (short)6));
					HSSFCell cell_edu_7 = row_edu.createCell((short)7);
					list_peopleCell.add(cell_edu_7);
					cell_edu_7.setCellValue(eduvo.getMajor()==null?str:eduvo.getMajor());//רҵ
					sheet.addMergedRegion(new Region((short)(14+index), (short)7,(short)(14+index), (short)8));
					HSSFCell cell_edu_9 = row_edu.createCell((short)9);
					list_peopleCell.add(cell_edu_9);
					cell_edu_9.setCellValue(eduvo.getEducation()==null?str:map_defdoc.get(eduvo.getEducation()).toString());//ѧ��
					sheet.addMergedRegion(new Region((short)(14+index), (short)9,(short)(14+index), (short)9));
					HSSFCell cell_edu_10 = row_edu.createCell((short)10);
					list_peopleCell.add(cell_edu_10);
					cell_edu_10.setCellValue(eduvo.getDegree()==null?str:map_defdoc.get(eduvo.getDegree()).toString());//ѧλ
					sheet.addMergedRegion(new Region((short)(14+index), (short)10,(short)(14+index), (short)11));
				}
			}
			if(index == 0){
				index = index+1;
				HSSFRow row_14_wu = sheet.createRow(14+index);
				list_row.add(row_14_wu);
				HSSFCell cell_14_wu = row_14_wu.createCell((short)0);
				list_peopleCell.add(cell_14_wu);
				cell_14_wu.setCellValue(str);//
				sheet.addMergedRegion(new Region((short)(14+index), (short)0,(short)(14+index), (short)11));
			}
		}
		/*********************************��������end*******************************/
		
		
		/*********************************��������begin*******************************/
		
		HSSFRow row_work = sheet.createRow(15+index);
		list_row.add(row_work);
		HSSFCell cell_work_0 = row_work.createCell((short)0);
		list_bodyHeadCell.add(cell_work_0);
		cell_work_0.setCellValue("�� Ҫ �� �� �� �����ӵ�ǰ������λ��ʼ��ʱ�䵹��˳����д��");//
		sheet.addMergedRegion(new Region((short)(15+index), (short)0,(short)(15+index), (short)11));
		HSSFRow row_work_detail = sheet.createRow(16+index);
		list_row.add(row_work_detail);
		HSSFCell cell_detail_0 = row_work_detail.createCell((short)0);
		list_cell.add(cell_detail_0);
		cell_detail_0.setCellValue("��ʼʱ��");//
		sheet.addMergedRegion(new Region((short)16+index, (short)0,(short)16+index, (short)0));
		HSSFCell cell_detail_1 = row_work_detail.createCell((short)1);
		list_cell.add(cell_detail_1);
		cell_detail_1.setCellValue("��ֹʱ��");//
		sheet.addMergedRegion(new Region((short)16+index, (short)1,(short)16+index, (short)1));
		HSSFCell cell_detail_2 = row_work_detail.createCell((short)2);
		list_cell.add(cell_detail_2);
		cell_detail_2.setCellValue("��λ����");//
		sheet.addMergedRegion(new Region((short)16+index, (short)2,(short)16+index, (short)5));
		HSSFCell cell_detail_6 = row_work_detail.createCell((short)6);
		list_cell.add(cell_detail_6);
		cell_detail_6.setCellValue("����");//
		sheet.addMergedRegion(new Region((short)16+index, (short)6,(short)16+index, (short)7));
		HSSFCell cell_detail_8 = row_work_detail.createCell((short)8);
		list_cell.add(cell_detail_8);
		cell_detail_8.setCellValue("��λ");//
		sheet.addMergedRegion(new Region((short)16+index, (short)8,(short)16+index, (short)9));
		HSSFCell cell_detail_10 = row_work_detail.createCell((short)10);
		list_cell.add(cell_detail_10);
		cell_detail_10.setCellValue("��Ҫְ��");//
		sheet.addMergedRegion(new Region((short)16+index, (short)10,(short)16+index, (short)11));
		if(bvos.length>0){
			int workdetail =0;
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMPsnWorkVO".equals(classname)){
					index=index+1;
					workdetail =workdetail +1;
					RMPsnWorkVO workvo = (RMPsnWorkVO) bvos[i];
					HSSFRow row_detail = sheet.createRow(16+index);
					list_row.add(row_detail);
					HSSFCell cell_workdetail_0 = row_detail.createCell((short)0);
					list_peopleCell.add(cell_workdetail_0);
					cell_workdetail_0.setCellValue(workvo.getBegindate()==null?str:workvo.getBegindate()+"");//��ʼʱ��
					sheet.addMergedRegion(new Region((short)(16+index), (short)0,(short)(16+index), (short)0));
					HSSFCell cell_workdetail_1 = row_detail.createCell((short)1);
					list_peopleCell.add(cell_workdetail_1);
					cell_workdetail_1.setCellValue(workvo.getEnddate()==null?str:workvo.getEnddate()+"");//����ʱ��
					sheet.addMergedRegion(new Region((short)(16+index), (short)1,(short)(16+index), (short)1));
					HSSFCell cell_workdetail_2 = row_detail.createCell((short)2);
					list_peopleCell.add(cell_workdetail_2);
					cell_workdetail_2.setCellValue(workvo.getWorkcorp()==null?str:workvo.getWorkcorp());//��λ����
					sheet.addMergedRegion(new Region((short)(16+index), (short)2,(short)(16+index), (short)5));
					HSSFCell cell_workdetail_6 = row_detail.createCell((short)6);
					list_peopleCell.add(cell_workdetail_6);
					cell_workdetail_6.setCellValue(workvo.getWorkdept()==null?str:workvo.getWorkdept());//����
					sheet.addMergedRegion(new Region((short)(16+index), (short)6,(short)(16+index), (short)7));
					HSSFCell cell_workdetail_8 = row_detail.createCell((short)8);
					list_peopleCell.add(cell_workdetail_8);
					cell_workdetail_8.setCellValue(workvo.getAttributeValue("glbdef1")==null?str:workvo.getAttributeValue("glbdef1").toString());//��λ
					sheet.addMergedRegion(new Region((short)(16+index), (short)8,(short)(16+index), (short)9));
					HSSFCell cell_workdetail_10 = row_detail.createCell((short)10);
					list_peopleCell.add(cell_workdetail_10);
					cell_workdetail_10.setCellValue(workvo.getWorkduty()==null?str:workvo.getWorkduty());//ְ��
					sheet.addMergedRegion(new Region((short)(16+index), (short)10,(short)(16+index), (short)11));
				}
			}
			if(workdetail == 0){
				index=index+1;
				HSSFRow row_work_wu = sheet.createRow(16+index);
				list_row.add(row_work_wu);
				HSSFCell cell_work_wu = row_work_wu.createCell((short)0);
				list_peopleCell.add(cell_work_wu);
				cell_work_wu.setCellValue(str);//
				sheet.addMergedRegion(new Region((short)(16+index), (short)0,(short)(16+index), (short)11));
			}
		}
		/*********************************��������end*******************************/
		
		
		/*********************************��ѵ����begin*******************************/
		
		HSSFRow row_train = sheet.createRow(17+index);
		list_row.add(row_train);
		HSSFCell cell_train_h = row_train.createCell((short)0);
		list_bodyHeadCell.add(cell_train_h);
		cell_train_h.setCellValue("��Ҫ��ѵ����");//
		sheet.addMergedRegion(new Region((short)(17+index), (short)0,(short)(17+index), (short)11));
		HSSFRow row_train_b = sheet.createRow(18+index);
		list_row.add(row_train_b);
		HSSFCell cell_train_b0 = row_train_b.createCell((short)0);
		list_cell.add(cell_train_b0);
		cell_train_b0.setCellValue("��ʼʱ��");//
		sheet.addMergedRegion(new Region((short)(18+index), (short)0,(short)(18+index), (short)0));
		HSSFCell cell_train_b1 = row_train_b.createCell((short)1);
		list_cell.add(cell_train_b1);
		cell_train_b1.setCellValue("��ֹʱ��");//
		sheet.addMergedRegion(new Region((short)(18+index), (short)1,(short)(18+index), (short)1));
		HSSFCell cell_train_b2 = row_train_b.createCell((short)2);
		list_cell.add(cell_train_b2);
		cell_train_b2.setCellValue("��ѵ��Ŀ");//
		sheet.addMergedRegion(new Region((short)(18+index), (short)2,(short)(18+index), (short)5));
		HSSFCell cell_train_b6 = row_train_b.createCell((short)6);
		list_cell.add(cell_train_b6);
		cell_train_b6.setCellValue("��ѵ����");//
		sheet.addMergedRegion(new Region((short)(18+index), (short)6,(short)(18+index), (short)7));
		HSSFCell cell_train_b8 = row_train_b.createCell((short)8);
		list_cell.add(cell_train_b8);
		cell_train_b8.setCellValue("��ѵ����");//
		sheet.addMergedRegion(new Region((short)(18+index), (short)8,(short)(18+index), (short)9));
		HSSFCell cell_train_b10 = row_train_b.createCell((short)10);
		list_cell.add(cell_train_b10);
		cell_train_b10.setCellValue("��ȡ�ʸ�");//
		sheet.addMergedRegion(new Region((short)(18+index), (short)10,(short)(18+index), (short)11));
		if(bvos.length>0){
			int workdetail =0;
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMTrainVO".equals(classname)){
					index=index+1;
					workdetail =workdetail +1;
					RMTrainVO trainvo = (RMTrainVO) bvos[i];
					HSSFRow row_train_detail = sheet.createRow(18+index);
					list_row.add(row_train_detail);
					HSSFCell cell_traindetail_0 = row_train_detail.createCell((short)0);
					list_peopleCell.add(cell_traindetail_0);
					cell_traindetail_0.setCellValue(trainvo.getBegindate()==null?str:trainvo.getBegindate()+"");//��ʼʱ��
					sheet.addMergedRegion(new Region((short)(18+index), (short)0,(short)(18+index), (short)0));
					HSSFCell cell_traindetail_1 = row_train_detail.createCell((short)1);
					list_peopleCell.add(cell_traindetail_1);
					cell_traindetail_1.setCellValue(trainvo.getEnddate()==null?str:trainvo.getEnddate()+"");//����ʱ��
					sheet.addMergedRegion(new Region((short)(18+index), (short)1,(short)(18+index), (short)1));
					HSSFCell cell_traindetail_2 = row_train_detail.createCell((short)2);
					list_peopleCell.add(cell_traindetail_2);
					cell_traindetail_2.setCellValue(trainvo.getName()==null?str:trainvo.getName());//��ѵ��Ŀ
					sheet.addMergedRegion(new Region((short)(18+index), (short)2,(short)(18+index), (short)5));
					HSSFCell cell_traindetail_6 = row_train_detail.createCell((short)6);
					list_peopleCell.add(cell_traindetail_6);
					cell_traindetail_6.setCellValue(trainvo.getContent()==null?str:trainvo.getContent());//��ѵ����
					sheet.addMergedRegion(new Region((short)(18+index), (short)6,(short)(18+index), (short)7));
					HSSFCell cell_traindetail_8 = row_train_detail.createCell((short)8);
					list_peopleCell.add(cell_traindetail_8);
					cell_traindetail_8.setCellValue(trainvo.getAttributeValue("glbdef3")==null?str:trainvo.getAttributeValue("glbdef3").toString());//��ѵ����
					sheet.addMergedRegion(new Region((short)(18+index), (short)8,(short)(18+index), (short)9));
					HSSFCell cell_traindetail_10 = row_train_detail.createCell((short)10);
					list_peopleCell.add(cell_traindetail_10);
					cell_traindetail_10.setCellValue(trainvo.getAttributeValue("glbdef2")==null?str:trainvo.getAttributeValue("glbdef2").toString());//��ȡ�ʸ�
					sheet.addMergedRegion(new Region((short)(18+index), (short)10,(short)(18+index), (short)11));
				}
			}
			if(workdetail == 0){
				index=index+1;
				HSSFRow row_train_wu = sheet.createRow(18+index);
				list_row.add(row_train_wu);
				HSSFCell cell_train_wu = row_train_wu.createCell((short)0);
				list_peopleCell.add(cell_train_wu);
				cell_train_wu.setCellValue(str);//
				sheet.addMergedRegion(new Region((short)(18+index), (short)0,(short)(18+index), (short)11));
			}
		}
		/*********************************��ѵ����end*******************************/
		
		/*********************************��ͥ���begin*******************************/
		
		HSSFRow row_home = sheet.createRow(19+index);
		list_row.add(row_home);
		HSSFCell cell_home_h = row_home.createCell((short)0);
		list_bodyHeadCell.add(cell_home_h);
		cell_home_h.setCellValue("��ͥ���");//
		sheet.addMergedRegion(new Region((short)(19+index), (short)0,(short)(19+index), (short)11));
		HSSFRow row_home_b = sheet.createRow(20+index);
		list_row.add(row_home_b);
		HSSFCell cell_home_b0 = row_home_b.createCell((short)0);
		list_cell.add(cell_home_b0);
		cell_home_b0.setCellValue("����");//
		sheet.addMergedRegion(new Region((short)(20+index), (short)0,(short)(20+index), (short)0));
		HSSFCell cell_home_b1 = row_home_b.createCell((short)1);
		list_cell.add(cell_home_b1);
		cell_home_b1.setCellValue("�뱾�˹�ϵ");//
		sheet.addMergedRegion(new Region((short)(20+index), (short)1,(short)(20+index), (short)2));
		HSSFCell cell_home_b2 = row_home_b.createCell((short)3);
		list_cell.add(cell_home_b2);
		cell_home_b2.setCellValue("����");//
		sheet.addMergedRegion(new Region((short)(20+index), (short)3,(short)(20+index), (short)4));
		HSSFCell cell_home_b5 = row_home_b.createCell((short)5);
		list_cell.add(cell_home_b5);
		cell_home_b5.setCellValue("������λ");//
		sheet.addMergedRegion(new Region((short)(20+index), (short)5,(short)(20+index), (short)11));
		if(bvos.length>0){
			int workdetail =0;
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMFamilyVO".equals(classname)){
					index=index+1;
					workdetail =workdetail +1;
					RMFamilyVO familyvo = (RMFamilyVO) bvos[i];
					HSSFRow row_family_detail = sheet.createRow(20+index);
					list_row.add(row_family_detail);
					HSSFCell cell_familydetail_0 = row_family_detail.createCell((short)0);
					list_peopleCell.add(cell_familydetail_0);
					cell_familydetail_0.setCellValue(familyvo.getMem_name()==null?str:familyvo.getMem_name()+"");//����
					sheet.addMergedRegion(new Region((short)(20+index), (short)0,(short)(20+index), (short)0));
					HSSFCell cell_faimilydetail_1 = row_family_detail.createCell((short)1);
					list_peopleCell.add(cell_faimilydetail_1);
					cell_faimilydetail_1.setCellValue(familyvo.getMem_relation()==null?str:
						map_defdoc.get(familyvo.getMem_relation()).toString());//�뱾�˹�ϵ
					sheet.addMergedRegion(new Region((short)(20+index), (short)1,(short)(20+index), (short)2));
					HSSFCell cell_familydetail_3 = row_family_detail.createCell((short)3);
					list_peopleCell.add(cell_familydetail_3);
					int age = 0;
					if(familyvo.getMem_birthday() != null && !"".equals(familyvo.getMem_birthday())){
						age = (new UFDate()).getYear() - familyvo.getMem_birthday().getYear();
					}
					cell_familydetail_3.setCellValue(familyvo.getAttributeValue("glbdef2")==null?str:familyvo.getAttributeValue("glbdef2").toString());//����
					sheet.addMergedRegion(new Region((short)(20+index), (short)3,(short)(20+index), (short)4));
					HSSFCell cell_familydetail_5 = row_family_detail.createCell((short)5);
					list_peopleCell.add(cell_familydetail_5);
					cell_familydetail_5.setCellValue(familyvo.getMem_corp()==null?str:familyvo.getMem_corp());//������λ
					sheet.addMergedRegion(new Region((short)(20+index), (short)5,(short)(20+index), (short)11));
				}
			}
			if(workdetail == 0){
				index=index+1;
				HSSFRow row_train_wu = sheet.createRow(20+index);
				list_row.add(row_train_wu);
				HSSFCell cell_train_wu = row_train_wu.createCell((short)0);
				list_peopleCell.add(cell_train_wu);
				cell_train_wu.setCellValue(str);//
				sheet.addMergedRegion(new Region((short)(20+index), (short)0,(short)(20+index), (short)11));
			}
		}
		/*********************************��ͥ���end*******************************/
		
        /*********************************�������begin*******************************/
		HSSFRow row_enc = sheet.createRow(21+index);
		list_row.add(row_enc);
		HSSFCell cell_enc_h = row_enc.createCell((short)0);
		list_bodyHeadCell.add(cell_enc_h);
		cell_enc_h.setCellValue("�������");//
		sheet.addMergedRegion(new Region((short)(21+index), (short)0,(short)(21+index), (short)11));
		HSSFRow row_enc_b = sheet.createRow(22+index);
		list_row.add(row_enc_b);
		HSSFCell cell_enc_b0 = row_enc_b.createCell((short)0);
		list_cell.add(cell_enc_b0);
		cell_enc_b0.setCellValue("����ʱ��");//
		sheet.addMergedRegion(new Region((short)(22+index), (short)0,(short)(22+index), (short)0));
		HSSFCell cell_enc_b1 = row_enc_b.createCell((short)1);
		list_cell.add(cell_enc_b1);
		cell_enc_b1.setCellValue("��������");//
		sheet.addMergedRegion(new Region((short)(22+index), (short)1,(short)(22+index), (short)6));
		HSSFCell cell_enc_b7 = row_enc_b.createCell((short)7);
		list_cell.add(cell_enc_b7);
		cell_enc_b7.setCellValue("���͵�λ");//
		sheet.addMergedRegion(new Region((short)(22+index), (short)7,(short)(22+index), (short)11));
		if(bvos.length>0){
			int workdetail =0;
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMEncVO".equals(classname)){
					index=index+1;
					workdetail =workdetail +1;
					RMEncVO encvo = (RMEncVO) bvos[i];
					HSSFRow row_enc_detail = sheet.createRow(22+index);
					list_row.add(row_enc_detail);
					HSSFCell cell_encdetail_0 = row_enc_detail.createCell((short)0);
					list_peopleCell.add(cell_encdetail_0);
					cell_encdetail_0.setCellValue(encvo.getVencourdate()==null?str:encvo.getVencourdate().toString());//����ʱ��
					sheet.addMergedRegion(new Region((short)(22+index), (short)0,(short)(22+index), (short)0));
					HSSFCell cell_encdetail_1 = row_enc_detail.createCell((short)1);
					list_peopleCell.add(cell_encdetail_1);
					cell_encdetail_1.setCellValue(encvo.getVencourtype()==null?str:encvo.getVencourtype());//��������
					sheet.addMergedRegion(new Region((short)(22+index), (short)1,(short)(22+index), (short)6));
					HSSFCell cell_encdetail_5 = row_enc_detail.createCell((short)7);
					list_peopleCell.add(cell_encdetail_5);
					cell_encdetail_5.setCellValue(encvo.getVencourunit()==null?str:encvo.getVencourunit());//���͵�λ
					sheet.addMergedRegion(new Region((short)(22+index), (short)7,(short)(22+index), (short)11));
				}
			}
			if(workdetail == 0){
				index=index+1;
				HSSFRow row_train_wu = sheet.createRow(22+index);
				list_row.add(row_train_wu);
				HSSFCell cell_train_wu = row_train_wu.createCell((short)0);
				list_peopleCell.add(cell_train_wu);
				cell_train_wu.setCellValue(str);//
				sheet.addMergedRegion(new Region((short)(22+index), (short)0,(short)(22+index), (short)11));
			}
		}
		/*********************************�������end*******************************/
		
		/*********************************��Ҫ����ҵ��start*******************************/
		HSSFRow row_yj = sheet.createRow(23+index);
		//list_row.add(row_yj);
		row_yj.setHeight((short) 2000);
		HSSFCell cell_yj_h = row_yj.createCell((short)0);
		list_cell.add(cell_yj_h);
		cell_yj_h.setCellValue("��Ҫ����ҵ��");//
		sheet.addMergedRegion(new Region((short)(23+index), (short)0,(short)(23+index), (short)0));
		HSSFCell cell_yj_hh = row_yj.createCell((short)1);
		//list_cell.add(cell_yj_hh);
		cell_yj_hh.setCellValue(psnvo.getAttributeValue("glbdef27")==null?str:psnvo.getAttributeValue("glbdef27").toString());//
		sheet.addMergedRegion(new Region((short)(23+index), (short)1,(short)(23+index), (short)11));
		/*********************************��Ҫ����ҵ��end*******************************/
		
		/*********************************��Ҫ����ҵ��start*******************************/
		HSSFRow row_zwpj = sheet.createRow(24+index);
		//list_row.add(row_yj);
		row_zwpj.setHeight((short) 2000);
		HSSFCell cell_zwpj_h = row_zwpj.createCell((short)0);
		list_cell.add(cell_zwpj_h);
		cell_zwpj_h.setCellValue("��������");//

		sheet.addMergedRegion(new Region((short)(24+index), (short)0,(short)(24+index), (short)0));
		HSSFCell cell_zwpj_hh = row_zwpj.createCell((short)1);
		//list_cell.add(cell_zwpj_hh);
		cell_zwpj_hh.setCellValue(psnvo.getEvaluation()==null?str:psnvo.getEvaluation().toString());//
		sheet.addMergedRegion(new Region((short)(24+index), (short)1,(short)(24+index), (short)11));
		/*********************************��Ҫ����ҵ��end*******************************/
		
		/*********************************��������start*******************************/
		HSSFRow row_brsm = sheet.createRow(25+index);
		list_row.add(row_brsm);
		HSSFCell cell_brsm_h = row_brsm.createCell((short)0);
		list_cell.add(cell_brsm_h);
		cell_brsm_h.setCellValue("��������");//
		sheet.addMergedRegion(new Region((short)(25+index), (short)0,(short)(26+index), (short)0));
		HSSFCell cell_brsm_1 = row_brsm.createCell((short)1);
		list_cell.add(cell_brsm_1);
		cell_brsm_1.setCellValue("�Ա���ӦƸ���ύ����֤�顢���ϵ�ԭ���͸�ӡ������ȫ��ʵ��Ч�ģ���������������ʵ��������٣�����Ը��е���Ӧ�������Σ�������ӦƸ��λ�Ĵ���");//
		sheet.addMergedRegion(new Region((short)(25+index), (short)1,(short)(25+index), (short)11));
		HSSFRow row_brsm_2 = sheet.createRow(26+index);
		list_row.add(row_brsm_2);
		HSSFCell cell_brsm_2 = row_brsm_2.createCell((short)1);
		cell_brsm_2.setCellValue("����ǩ����            ʱ��:    ��       ��        ��");		
		sheet.addMergedRegion(new Region((short)(26+index), (short)1,(short)(26+index), (short)11));
					
		/*********************************��������end*******************************/
		
		
		/*********************************��עstart*******************************/
		HSSFRow row_memo_1 = sheet.createRow(27+index);
		row_memo_1.setHeight((short)500);
		HSSFCell cell_memo_1 = row_memo_1.createCell((short)0);
		list_cell.add(cell_memo_1);
		cell_memo_1.setCellValue("ע�������ݽ϶�ɽ��������������Ҫ�ı�ԭ��Ԫ���п�û�е�������ޡ�,�ൺ�������Ź�����http://qd-metro.com");//
		setRegionStyle(sheet,new Region(27+index,(short)0,27+index,(short)11),bodyStyle);
		sheet.addMergedRegion(new Region((short)(27+index), (short)0,(short)(27+index), (short)11));
		
		/*********************************��ע����end*******************************/
		
		for (int i = 2; i < sheet.getLastRowNum(); i++) {
			setRegionStyle(sheet,new Region(i,(short)0,i,(short)11),bodyStyle);
		}
		//�����и�
		setRowH(list_row);
		//���ñ��塢�����ͷ��Ԫ���ʽ
		setMutiCellStyle(workbook,list_cell,getBodyStyle(workbook));
		setMutiCellStyle(workbook,list_peopleCell,getBodyByPeopleStyle(workbook));
		cell_yj_hh.setCellStyle(getBodyByJLStyle(workbook));
		cell_zwpj_hh.setCellStyle(getBodyByJLStyle(workbook));
		setMutiCellStyle(workbook,list_bodyHeadCell,getBodyHeadStyle(workbook));
	}
	
	/**
	 * ����Excel
	 * 
	 * @param sheet
	 * @param workbook
	 * @param aggvo 
	 * @param map_defdoc 
	 * @param job 
	 * @param dept 
	 * @param org 
	 */
	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	private  void createSchlExcel(HSSFSheet sheet, HSSFWorkbook workbook, AggRMPsndocVO aggvo,
			String org, String dept, String job, Map map_defdoc,Map map_region) {
		HSSFCellStyle headStyle =getHeadStyle(workbook);
		HSSFCellStyle headDateStyle =getHeadDateStyle(workbook);
		HSSFCellStyle bodyHeadStyle =getBodyHeadStyle(workbook);
		HSSFCellStyle bodyStyle =getBodyStyle(workbook);	
		
		/***
		 * �����п�
		 */
		sheet.setColumnWidth(0,2400);//A
		sheet.setColumnWidth(1,2400);
		sheet.setColumnWidth(2,2200);//C
		sheet.setColumnWidth(3,800);
		sheet.setColumnWidth(4,2200);//E
		sheet.setColumnWidth(5,3300);
		sheet.setColumnWidth(6,3500);//G
		sheet.setColumnWidth(7,2800);
		sheet.setColumnWidth(8,2300);//I
		sheet.setColumnWidth(9,2400);	
		
		sheet.setMargin(HSSFSheet.TopMargin, (double)0.5);
		sheet.setMargin(HSSFSheet.BottomMargin,(double)0.5);
		sheet.setMargin(HSSFSheet.LeftMargin, (double)0.4);
		sheet.setMargin(HSSFSheet.RightMargin,(double)0.4);
		//sheet.setVerticallyCenter(true);
		sheet.setHorizontallyCenter(true);
		sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
		
		String str = "��";
		
		
		RMPsndocVO psnvo = aggvo.getPsndocVO();//ӦƸ�Ǽ���Ա��ϢVO	
		List list_cell = new ArrayList<HSSFCell>();//���嵥Ԫ��
		List list_peopleCell = new ArrayList<HSSFCell>();//�����ֶ�д�뵥Ԫ��
		List list_bodyHeadCell = new ArrayList<HSSFCell>();//�����ͷ��Ԫ��
		List list_row = new ArrayList<HSSFCell>();//��
		/****����****/
		HSSFRow row_0 = sheet.createRow(0);
		row_0.setHeight((short)700);
		HSSFCell cell_name = row_0.createCell((short)0);
		cell_name.setCellValue(org+"ӦƸ��Ϣ��(У԰��Ƹ)");		
		cell_name.setCellStyle(headStyle);
		setRegionStyle(sheet,new Region(0,(short)0,0,(short)9),headStyle);
		sheet.addMergedRegion(new Region(0, (short)0,0, (short)9));
		
		/*********************************������Ϣbegin*******************************/
		/***�������***/
		HSSFRow row_1 = sheet.createRow(1);
		row_1.setHeight((short)400);
		HSSFCell celldate = row_1.createCell((short)0);
		celldate.setCellValue("������ڣ�"+(new UFDate()).getYear()+" ��  "+(new UFDate()).getMonth()+" �� "+(new UFDate()).getDay()+" ��");		
		celldate.setCellStyle(headDateStyle);
		setRegionStyle(sheet,new Region(1,(short)0,1,(short)9),headDateStyle);
		sheet.addMergedRegion(new Region((short)1, (short)0,(short)1, (short)9));
		
		/***ӦƸ��λ***/
		HSSFRow row_2 = sheet.createRow(2);
		list_row.add(row_2);
		HSSFCell cell_2_0 = row_2.createCell((short)0);
		list_cell.add(cell_2_0);
		cell_2_0.setCellValue("ӦƸ��λ");
		sheet.addMergedRegion(new Region(2, (short)0,3, (short)0));
		
		HSSFCell cell_2_1 = row_2.createCell((short)1);
		list_cell.add(cell_2_1);
		cell_2_1.setCellValue("����");
		sheet.addMergedRegion(new Region(2, (short)1,2, (short)2));
		
		HSSFCell cell_2_3 = row_2.createCell((short)3);
		list_cell.add(cell_2_3);
		cell_2_3.setCellValue("����/��");
		setRegionStyle(sheet,new Region(2,(short)3,2,(short)4),bodyStyle);
		sheet.addMergedRegion(new Region(2, (short)3,2, (short)4));
		HSSFCell cell_2_5 = row_2.createCell((short)5);
		list_cell.add(cell_2_5);
		cell_2_5.setCellValue("��λ");
		sheet.addMergedRegion(new Region(2, (short)5,2, (short)5));
		HSSFCell cell_2_7 = row_2.createCell((short)6);
		list_cell.add(cell_2_7);
		cell_2_7.setCellValue("��������");
		sheet.addMergedRegion(new Region(2, (short)6,2, (short)6));
		HSSFCell cell_2_9 = row_2.createCell((short)7);
		list_cell.add(cell_2_9);
		cell_2_9.setCellValue("");//��������
		
		Object photo = psnvo.getPhoto();
		byte[] pngBytes = null;
		if(photo==null){	
			HSSFCell cell_2_8 = row_2.createCell((short)8);
			list_cell.add(cell_2_8);
			cell_2_8.setCellValue("����Ƭ");//��Ƭ
			sheet.addMergedRegion(new Region(2, (short)8,6, (short)9));			
		}else{			
			photo = VOUtils.transPreviewPhoto((byte[]) photo);
			pngBytes = (byte[]) photo;
			HSSFPatriarch patriarch = sheet.createDrawingPatriarch(); 
			insertImage(workbook, patriarch, pngBytes, 2, 8, 7, 10,0);		
	    }
				    								
		int dex = dept.indexOf("-");
		HSSFRow row_3 = sheet.createRow(3);
		list_row.add(row_3);
		sheet.addMergedRegion(new Region(3, (short)1,3, (short)2));
		HSSFCell cell_3_1 = row_3.createCell((short)1);
		list_peopleCell.add(cell_3_1);
		cell_3_1.setCellValue(dept==null?str:(dex==-1?dept:dept.substring(0,dex)));//����
		sheet.addMergedRegion(new Region(3, (short)3,3, (short)4));
		HSSFCell cell_3_3 = row_3.createCell((short)3);
		list_peopleCell.add(cell_3_3);
		cell_3_3.setCellValue(dept==null?str:(dex==-1?str:dept.substring(dex+1)));//����
		sheet.addMergedRegion(new Region(3, (short)5,3, (short)5));
		HSSFCell cell_3_5 = row_3.createCell((short)5);
		list_peopleCell.add(cell_3_5);
		cell_3_5.setCellValue(job==null?str:job);//��λ
		sheet.addMergedRegion(new Region(3, (short)6,3, (short)6));
		HSSFCell cell_3_6 = row_3.createCell((short)6);
		list_cell.add(cell_3_6);
		cell_3_6.setCellValue("�Ƿ���Ӹ�λ����");
		sheet.addMergedRegion(new Region(3, (short)7,3, (short)7));
		HSSFCell cell_3_7 = row_3.createCell((short)7);
		list_peopleCell.add(cell_3_7);
		cell_3_7.setCellValue(psnvo.getAttributeValue("glbdef25")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef25")).toString());//�Ƿ���Ӹ�λ����
		  
		
		HSSFRow row_4 = sheet.createRow(4);
		list_row.add(row_4);
		sheet.addMergedRegion(new Region(4, (short)0,4, (short)0));
		HSSFCell cell_4_0 = row_4.createCell((short)0);
		list_cell.add(cell_4_0);
		cell_4_0.setCellValue("����");
		sheet.addMergedRegion(new Region(4, (short)1,4, (short)2));
		HSSFCell cell_4_1 = row_4.createCell((short)1);
		list_peopleCell.add(cell_4_1);
	    cell_4_1.setCellValue(psnvo.getName()==null?str:psnvo.getName());
		sheet.addMergedRegion(new Region(4, (short)3,4, (short)4));
		HSSFCell cell_4_3 = row_4.createCell((short)3);
		list_cell.add(cell_4_3);
		cell_4_3.setCellValue("�Ա�");
		sheet.addMergedRegion(new Region(4, (short)5,4, (short)5));
		HSSFCell cell_4_5 = row_4.createCell((short)5);
		list_peopleCell.add(cell_4_5);
		cell_4_5.setCellValue(psnvo.getSex()==0?str:(psnvo.getSex()==1?"��":"Ů"));
		sheet.addMergedRegion(new Region(4, (short)6,4, (short)6));
		HSSFCell cell_4_6 = row_4.createCell((short)6);
		list_cell.add(cell_4_6);
		cell_4_6.setCellValue("����");
		sheet.addMergedRegion(new Region(4, (short)7,4, (short)7));
		HSSFCell cell_4_7 = row_4.createCell((short)7);
		list_peopleCell.add(cell_4_7);
		cell_4_7.setCellValue(psnvo.getNationality()==null?str:map_defdoc.get(psnvo.getNationality()).toString());
		
		
		HSSFRow row_5 = sheet.createRow(5);
		list_row.add(row_5);
		sheet.addMergedRegion(new Region(5, (short)0,5, (short)0));
		HSSFCell cell_5_0 = row_5.createCell((short)0);
		list_cell.add(cell_5_0);
		cell_5_0.setCellValue("��������");
		sheet.addMergedRegion(new Region(5, (short)1,5, (short)2));
		HSSFCell cell_5_1 = row_5.createCell((short)1);
		list_peopleCell.add(cell_5_1);
		cell_5_1.setCellValue(psnvo.getBirthdate().toString()==null?str:psnvo.getBirthdate().toString());
		sheet.addMergedRegion(new Region(5, (short)3,5, (short)4));
		HSSFCell cell_5_3 = row_5.createCell((short)3);
		list_cell.add(cell_5_3);
		cell_5_3.setCellValue("����");
		sheet.addMergedRegion(new Region(5, (short)5,5, (short)5));
		HSSFCell cell_5_5 = row_5.createCell((short)5);
		list_peopleCell.add(cell_5_5);
		cell_5_5.setCellValue(psnvo.getNativeplace()==null?str:map_region.get(psnvo.getNativeplace()).toString());
		sheet.addMergedRegion(new Region(5, (short)6,5, (short)6));
		HSSFCell cell_5_6 = row_5.createCell((short)6);
		list_cell.add(cell_5_6);
		cell_5_6.setCellValue("��Դ��");
		sheet.addMergedRegion(new Region(5, (short)7,5, (short)7));
		HSSFCell cell_5_7 = row_5.createCell((short)7);
		list_peopleCell.add(cell_5_7);
		cell_5_7.setCellValue(psnvo.getPermanreside()==null?str:map_region.get(psnvo.getPermanreside()).toString());
		
		
		HSSFRow row_6 = sheet.createRow(6);
		list_row.add(row_6);
		sheet.addMergedRegion(new Region(6, (short)0,6, (short)0));
		HSSFCell cell_6_0 = row_6.createCell((short)0);
		list_cell.add(cell_6_0);
		cell_6_0.setCellValue("����ߣ�cm��");
		sheet.addMergedRegion(new Region(6, (short)1,6, (short)2));
		HSSFCell cell_6_1 = row_6.createCell((short)1);
		list_peopleCell.add(cell_6_1);
		cell_6_1.setCellValue(psnvo.getAttributeValue("glbdef2")==null?str:psnvo.getAttributeValue("glbdef2").toString());
		sheet.addMergedRegion(new Region(6, (short)3,6, (short)4));
		HSSFCell cell_6_3 = row_6.createCell((short)3);
		list_cell.add(cell_6_3);
		cell_6_3.setCellValue("���أ�kg��");
		sheet.addMergedRegion(new Region(6, (short)5,6, (short)5));
		HSSFCell cell_6_5 = row_6.createCell((short)5);
		list_peopleCell.add(cell_6_5);
		cell_6_5.setCellValue(psnvo.getAttributeValue("glbdef4")==null?str:psnvo.getAttributeValue("glbdef4").toString());
		sheet.addMergedRegion(new Region(6, (short)6,6, (short)6));
		HSSFCell cell_6_6 = row_6.createCell((short)6);
		list_cell.add(cell_6_6);
		cell_6_6.setCellValue("������ò");
		sheet.addMergedRegion(new Region(6, (short)7,6, (short)7));
		HSSFCell cell_6_7 = row_6.createCell((short)7);
		list_peopleCell.add(cell_6_7);
		cell_6_7.setCellValue(psnvo.getPolity()==null?str:map_defdoc.get(psnvo.getPolity()).toString());
		
		
		
		HSSFRow row_7 = sheet.createRow(7);
		list_row.add(row_7);
		sheet.addMergedRegion(new Region(7, (short)0,7, (short)0));
		HSSFCell cell_7_0 = row_7.createCell((short)0);
		list_cell.add(cell_7_0);
		cell_7_0.setCellValue("������������/�ң�");
		sheet.addMergedRegion(new Region(7, (short)1,7, (short)2));
		HSSFCell cell_7_1 = row_7.createCell((short)1);
		list_peopleCell.add(cell_7_1);
		cell_7_1.setCellValue(psnvo.getAttributeValue("glbdef3")==null?str:psnvo.getAttributeValue("glbdef3").toString());
		sheet.addMergedRegion(new Region(7, (short)3,7, (short)4));
		HSSFCell cell_7_3 = row_7.createCell((short)3);
		list_cell.add(cell_7_3);
		cell_7_3.setCellValue("ɫä/ɫ��");
		sheet.addMergedRegion(new Region(7, (short)5,7, (short)5));
		HSSFCell cell_7_5 = row_7.createCell((short)5);
		list_peopleCell.add(cell_7_5);
		cell_7_5.setCellValue(psnvo.getAttributeValue("glbdef17")==null?str:map_defdoc.get(psnvo.getAttributeValue("glbdef17")).toString());
		sheet.addMergedRegion(new Region(7, (short)6,7, (short)6));
		HSSFCell cell_7_6 = row_7.createCell((short)6);
		list_cell.add(cell_7_6);
		cell_7_6.setCellValue("����״��");
		sheet.addMergedRegion(new Region(7, (short)7,7, (short)9));
		HSSFCell cell_7_7 = row_7.createCell((short)7);
		list_peopleCell.add(cell_7_7);
		cell_7_7.setCellValue(psnvo.getHealth()==null?str:map_defdoc.get(psnvo.getHealth()).toString());
		
		HSSFRow row_8 = sheet.createRow(8);
		list_row.add(row_8);
		sheet.addMergedRegion(new Region(8, (short)0,8, (short)0));
		HSSFCell cell_8_0 = row_8.createCell((short)0);
		list_cell.add(cell_8_0);
		cell_8_0.setCellValue("��ͥסַ");
		sheet.addMergedRegion(new Region(8, (short)1,8, (short)2));
		HSSFCell cell_8_1 = row_8.createCell((short)1);
		list_peopleCell.add(cell_8_1);
		//cell_8_1.setCellValue(psnvo.getStudentposition()==null?str:psnvo.getStudentposition());
		cell_8_1.setCellValue(psnvo.getAttributeValue("glbdef15")==null?str:psnvo.getAttributeValue("glbdef15").toString());
		sheet.addMergedRegion(new Region(8, (short)3,8, (short)4));
		HSSFCell cell_8_3 = row_8.createCell((short)3);
		list_cell.add(cell_8_3);
		cell_8_3.setCellValue("�������");
		sheet.addMergedRegion(new Region(8, (short)5,8, (short)5));
		HSSFCell cell_8_5 = row_8.createCell((short)5);
		list_peopleCell.add(cell_8_5);
		cell_8_5.setCellValue(psnvo.getMarital()==null?str:map_defdoc.get(psnvo.getMarital()).toString());
		sheet.addMergedRegion(new Region(8, (short)6,8, (short)6));
		HSSFCell cell_8_6 = row_8.createCell((short)6);
		list_cell.add(cell_8_6);
		cell_8_6.setCellValue("���֤��");
		sheet.addMergedRegion(new Region(8, (short)7,8, (short)9));
		HSSFCell cell_8_7 = row_8.createCell((short)7);
		list_peopleCell.add(cell_8_7);
		cell_8_7.setCellValue(psnvo.getId()==null?str:psnvo.getId());
		
		
		HSSFRow row_9 = sheet.createRow(9);
		list_row.add(row_9);
		sheet.addMergedRegion(new Region(9, (short)0,9, (short)0));
		HSSFCell cell_9_0 = row_9.createCell((short)0);
		list_cell.add(cell_9_0);
		cell_9_0.setCellValue("��ϵ�绰");
		sheet.addMergedRegion(new Region(9, (short)1,9, (short)2));
		HSSFCell cell_9_1 = row_9.createCell((short)1);
		list_peopleCell.add(cell_9_1);
		cell_9_1.setCellValue(psnvo.getMobile()==null?str:psnvo.getMobile());
		sheet.addMergedRegion(new Region(9, (short)3,9, (short)4));
		HSSFCell cell_9_3 = row_9.createCell((short)3);
		list_cell.add(cell_9_3);
		cell_9_3.setCellValue("��������");
		sheet.addMergedRegion(new Region(9, (short)5,9, (short)5));
		HSSFCell cell_9_5 = row_9.createCell((short)5);
		list_peopleCell.add(cell_9_5);
		cell_9_5.setCellValue(psnvo.getEmail()==null?str:psnvo.getEmail());
		sheet.addMergedRegion(new Region(9, (short)6,9, (short)6));
		HSSFCell cell_9_6 = row_9.createCell((short)6);
		list_cell.add(cell_9_6);
		cell_9_6.setCellValue("�к��س�");
		sheet.addMergedRegion(new Region(9, (short)7,9, (short)9));
		HSSFCell cell_9_7 = row_9.createCell((short)7);
		list_peopleCell.add(cell_9_7);
		cell_9_7.setCellValue(psnvo.getPersonal()==null?str:psnvo.getPersonal());
		
		
		HSSFRow row_10 = sheet.createRow(10);
		list_row.add(row_10);
		sheet.addMergedRegion(new Region(10, (short)0,10, (short)0));
		HSSFCell cell_10_0 = row_10.createCell((short)0);
		list_cell.add(cell_10_0);
		cell_10_0.setCellValue("ȫ����ѧ��");
		sheet.addMergedRegion(new Region(10, (short)1,10, (short)2));
		HSSFCell cell_10_1 = row_10.createCell((short)1);
		list_peopleCell.add(cell_10_1);
		cell_10_1.setCellValue(psnvo.getEdu()==null?str:map_defdoc.get(psnvo.getEdu()).toString());
		sheet.addMergedRegion(new Region(10, (short)3,10, (short)4));
		HSSFCell cell_10_3 = row_10.createCell((short)3);
		list_cell.add(cell_10_3);
		cell_10_3.setCellValue("ȫ����ѧλ");
		sheet.addMergedRegion(new Region(10, (short)5,10, (short)5));
		HSSFCell cell_10_5 = row_10.createCell((short)5);
		list_peopleCell.add(cell_10_5);
		cell_10_5.setCellValue(psnvo.getPk_degree()==null?str:map_defdoc.get(psnvo.getPk_degree()).toString());
		sheet.addMergedRegion(new Region(10, (short)6,10, (short)6));
		HSSFCell cell_10_6 = row_10.createCell((short)6);
		list_cell.add(cell_10_6);
		cell_10_6.setCellValue("����ѧУְ��");
		sheet.addMergedRegion(new Region(10, (short)7,10, (short)9));
		HSSFCell cell_10_7 = row_10.createCell((short)7);
		list_peopleCell.add(cell_10_7);
		cell_10_7.setCellValue(psnvo.getAttributeValue("glbdef9")==null?str:psnvo.getAttributeValue("glbdef9").toString());
		
		
		HSSFRow row_11 = sheet.createRow(11);
		list_row.add(row_11);
		sheet.addMergedRegion(new Region(11, (short)0,11, (short)0));
		HSSFCell cell_11_0 = row_11.createCell((short)0);
		list_cell.add(cell_11_0);
		cell_11_0.setCellValue("ȫ���Ʊ�ҵԺУ");
		sheet.addMergedRegion(new Region(11, (short)1,11, (short)2));
		HSSFCell cell_11_1 = row_11.createCell((short)1);
		list_peopleCell.add(cell_11_1);
		cell_11_1.setCellValue(psnvo.getAttributeValue("glbdef13")==null?str:psnvo.getAttributeValue("glbdef13").toString());
		sheet.addMergedRegion(new Region(11, (short)3,11, (short)4));
		HSSFCell cell_11_3 = row_11.createCell((short)3);
		list_cell.add(cell_11_3);
		cell_11_3.setCellValue("ȫ����רҵ");
		sheet.addMergedRegion(new Region(11, (short)5,11, (short)5));
		HSSFCell cell_11_5 = row_11.createCell((short)5);
		list_peopleCell.add(cell_11_5);
		cell_11_5.setCellValue(psnvo.getAttributeValue("glbdef14")==null?str:psnvo.getAttributeValue("glbdef14").toString());
		sheet.addMergedRegion(new Region(11, (short)6,11, (short)6));
		HSSFCell cell_11_6 = row_11.createCell((short)6);
		list_cell.add(cell_11_6);
		cell_11_6.setCellValue("רҵ�ɼ�����(����/����)");
		sheet.addMergedRegion(new Region(11, (short)7,11, (short)9));
		HSSFCell cell_11_7 = row_11.createCell((short)7);
		list_peopleCell.add(cell_11_7);
		cell_11_7.setCellValue(psnvo.getAttributeValue("glbdef10")==null?str:psnvo.getAttributeValue("glbdef10").toString());
		
		
		HSSFRow row_12 = sheet.createRow(12);
		list_row.add(row_12);
		sheet.addMergedRegion(new Region(12, (short)0,12, (short)0));
		HSSFCell cell_12_0 = row_12.createCell((short)0);
		list_cell.add(cell_12_0);
		cell_12_0.setCellValue("Ӣ��ȼ�/�ɼ�");
		sheet.addMergedRegion(new Region(12, (short)1,12, (short)2));
		HSSFCell cell_12_1 = row_12.createCell((short)1);
		list_peopleCell.add(cell_12_1);
		cell_12_1.setCellValue(psnvo.getFroeignlang()==null?str:psnvo.getFroeignlang());
		sheet.addMergedRegion(new Region(12, (short)3,12, (short)4));
		HSSFCell cell_12_3 = row_12.createCell((short)3);
		list_cell.add(cell_12_3);
		cell_12_3.setCellValue("�����ˮƽ");
		sheet.addMergedRegion(new Region(12, (short)5,12, (short)5));
		HSSFCell cell_12_5 = row_12.createCell((short)5);
		list_peopleCell.add(cell_12_5);
		cell_12_5.setCellValue(psnvo.getComputerlevel()==null?str:psnvo.getComputerlevel());
		sheet.addMergedRegion(new Region(12, (short)6,12, (short)6));
		HSSFCell cell_12_6 = row_12.createCell((short)6);
		list_cell.add(cell_12_6);
		cell_12_6.setCellValue("�ѿ�ȡ֤��");
		sheet.addMergedRegion(new Region(12, (short)7,12, (short)9));
		HSSFCell cell_12_7 = row_12.createCell((short)7);
		list_peopleCell.add(cell_12_7);
		cell_12_7.setCellValue(psnvo.getAttributeValue("glbdef5")==null?str:psnvo.getAttributeValue("glbdef5").toString());
		
		
		HSSFRow row_13 = sheet.createRow(13);
		list_row.add(row_13);
		sheet.addMergedRegion(new Region(13, (short)0,13, (short)0));
		HSSFCell cell_13_0 = row_13.createCell((short)0);
		list_cell.add(cell_13_0);
		cell_13_0.setCellValue("���޿γ�");
		sheet.addMergedRegion(new Region(13, (short)1,13, (short)9));
		HSSFCell cell_13_1 = row_13.createCell((short)1);
		list_peopleCell.add(cell_13_1);
		cell_13_1.setCellValue(psnvo.getAttributeValue("glbdef16")==null?str:psnvo.getAttributeValue("glbdef16").toString());
		
		/*********************************������Ϣend*******************************/
		
		/*********************************��������begin*******************************/
		HSSFRow row_14 = sheet.createRow(14);
		list_row.add(row_14);
		HSSFCell cell_14_0 = row_14.createCell((short)0);
		list_bodyHeadCell.add(cell_14_0);
		cell_14_0.setCellValue("�� �� �� �����Ӹ��У���ר����ʼ��ѧ���ɵ͵���������д��");
		sheet.addMergedRegion(new Region(14, (short)0,14, (short)9));
	
		HSSFRow row_15 = sheet.createRow(15);
		list_row.add(row_15);
		HSSFCell cell_15_0 = row_15.createCell((short)0);
		list_cell.add(cell_15_0);
		cell_15_0.setCellValue("��ʼʱ��");//
		sheet.addMergedRegion(new Region(15, (short)0,15, (short)0));
		
		HSSFCell cell_15_1 = row_15.createCell((short)1);
		list_cell.add(cell_15_1);
		cell_15_1.setCellValue("��ҵʱ��");//
		sheet.addMergedRegion(new Region(15, (short)1,15, (short)1));
		HSSFCell cell_15_2 = row_15.createCell((short)2);
		list_cell.add(cell_15_2);
		cell_15_2.setCellValue("ѧУ����");//
		sheet.addMergedRegion(new Region(15, (short)2,15, (short)4));
		HSSFCell cell_15_5 = row_15.createCell((short)5);
		list_cell.add(cell_15_5);
		cell_15_5.setCellValue("��ѧרҵ");//
		sheet.addMergedRegion(new Region(15, (short)5,15, (short)5));
		HSSFCell cell_15_6 = row_15.createCell((short)6);
		list_cell.add(cell_15_6);
		cell_15_6.setCellValue("¼ȡ����");//
		sheet.addMergedRegion(new Region(15, (short)6,15, (short)6));
		HSSFCell cell_15_7 = row_15.createCell((short)7);
		list_cell.add(cell_15_7);
		cell_15_7.setCellValue("ѧ��");//
		sheet.addMergedRegion(new Region(15, (short)7,15, (short)7));
		HSSFCell cell_15_8 = row_15.createCell((short)8);
		list_cell.add(cell_15_8);
		cell_15_8.setCellValue("ѧ��");//
		sheet.addMergedRegion(new Region(15, (short)8,15, (short)8));
		HSSFCell cell_15_9 = row_15.createCell((short)9);
		list_cell.add(cell_15_9);
		cell_15_9.setCellValue("ѧλ");//
		sheet.addMergedRegion(new Region(15, (short)9,15, (short)9));
		//CircularlyAccessibleValueObject[] bvos = aggvo.getAllChildrenVO();
		CircularlyAccessibleValueObject[] bvos = aggvo.getTableVO("rm_psndoc_edu");
		bvos = getBvosBySort(bvos);
		int index=0;
		if(bvos.length>0){
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMEduVO".equals(classname)){
					index=index+1;
					RMEduVO eduvo = (RMEduVO) bvos[i];
					HSSFRow row_edu = sheet.createRow(15+index);
					list_row.add(row_edu);
					HSSFCell cell_edu_0 = row_edu.createCell((short)0);
					list_peopleCell.add(cell_edu_0);
					cell_edu_0.setCellValue(eduvo.getBegindate()==null?str:eduvo.getBegindate()+"");//��ʼʱ��
					sheet.addMergedRegion(new Region(15+index, (short)0,15+index, (short)0));
					
					HSSFCell cell_edu_1 = row_edu.createCell((short)1);
					list_peopleCell.add(cell_edu_1);
					cell_edu_1.setCellValue(eduvo.getEnddate()==null?str:eduvo.getEnddate()+"");//����ʱ��
					sheet.addMergedRegion(new Region(15+index, (short)1,15+index, (short)1));
					HSSFCell cell_edu_2 = row_edu.createCell((short)2);
					list_peopleCell.add(cell_edu_2);
					cell_edu_2.setCellValue(eduvo.getSchool()==null?str:eduvo.getSchool());//ѧУ
					sheet.addMergedRegion(new Region(15+index, (short)2,15+index, (short)4));
					HSSFCell cell_edu_5 = row_edu.createCell((short)5);
					list_peopleCell.add(cell_edu_5);
					cell_edu_5.setCellValue(eduvo.getMajor()==null?str:eduvo.getMajor());
					sheet.addMergedRegion(new Region(15+index, (short)5,15+index, (short)5));
					HSSFCell cell_edu_6 = row_edu.createCell((short)6);
					list_peopleCell.add(cell_edu_6);
					cell_edu_6.setCellValue(eduvo.getAttributeValue("glbdef3")==null?str:map_defdoc.get(eduvo.getAttributeValue("glbdef3").toString()).toString());
					sheet.addMergedRegion(new Region(15+index, (short)6,15+index, (short)6));
					HSSFCell cell_edu_7 = row_edu.createCell((short)7);
					list_peopleCell.add(cell_edu_7);
					cell_edu_7.setCellValue(eduvo.getAttributeValue("glbdef2")==null?str:map_defdoc.get(eduvo.getAttributeValue("glbdef2").toString()).toString());
					sheet.addMergedRegion(new Region(15+index, (short)7,15+index, (short)7));
					HSSFCell cell_edu_8 = row_edu.createCell((short)8);
					list_peopleCell.add(cell_edu_8);
					cell_edu_8.setCellValue(eduvo.getEducation()==null?str:map_defdoc.get(eduvo.getEducation()).toString());//ѧ��
					sheet.addMergedRegion(new Region(15+index, (short)8,15+index, (short)8));
					HSSFCell cell_edu_9 = row_edu.createCell((short)9);
					list_peopleCell.add(cell_edu_9);
					cell_edu_9.setCellValue(eduvo.getDegree()==null?str:map_defdoc.get(eduvo.getDegree()).toString());//ѧλ
					sheet.addMergedRegion(new Region(15+index, (short)9,15+index, (short)9));
				}
			}
			if(index == 0){
				index = index +1;
				HSSFRow row_16_wu = sheet.createRow(15+index);
				list_row.add(row_16_wu);
				HSSFCell cell_16_wu = row_16_wu.createCell((short)0);
				list_peopleCell.add(cell_16_wu);
				cell_16_wu.setCellValue(str);//
				sheet.addMergedRegion(new Region(15+index, (short)0,15+index, (short)9));
			}
		}
		/*********************************��������end*******************************/
		
		
		/*********************************��������begin*******************************/
		
		HSSFRow row_work = sheet.createRow(16+index);
		list_row.add(row_work);
		HSSFCell cell_work_0 = row_work.createCell((short)0);
		list_bodyHeadCell.add(cell_work_0);
		cell_work_0.setCellValue("��Ҫʵϰ��������ʱ���Զ������");//
		sheet.addMergedRegion(new Region(16+index, (short)0,16+index, (short)9));
		HSSFRow row_work_detail = sheet.createRow(17+index);
		list_row.add(row_work_detail);
		HSSFCell cell_detail_0 = row_work_detail.createCell((short)0);
		list_cell.add(cell_detail_0);
		cell_detail_0.setCellValue("��ʼʱ��");//
		sheet.addMergedRegion(new Region(17+index, (short)0,17+index, (short)0));
		HSSFCell cell_detail_1 = row_work_detail.createCell((short)1);
		list_cell.add(cell_detail_1);
		cell_detail_1.setCellValue("��ֹʱ��");//
		sheet.addMergedRegion(new Region(17+index, (short)1,17+index, (short)1));
		HSSFCell cell_detail_2 = row_work_detail.createCell((short)2);
		list_cell.add(cell_detail_2);
		cell_detail_2.setCellValue("��λ����");//
		sheet.addMergedRegion(new Region(17+index, (short)2,17+index, (short)4));
		HSSFCell cell_detail_5 = row_work_detail.createCell((short)5);
		list_cell.add(cell_detail_5);
		cell_detail_5.setCellValue("����ְλ");//
		sheet.addMergedRegion(new Region(17+index, (short)5,17+index, (short)5));
		HSSFCell cell_detail_6 = row_work_detail.createCell((short)6);
		list_cell.add(cell_detail_6);
		cell_detail_6.setCellValue("��Ҫ����");//
		sheet.addMergedRegion(new Region(17+index, (short)6,17+index, (short)9));
		if(bvos.length>0){
			int workdetail =0;
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMPsnWorkVO".equals(classname)){
					index=index+1;
					workdetail =workdetail +1;
					RMPsnWorkVO workvo = (RMPsnWorkVO) bvos[i];
					HSSFRow row_detail = sheet.createRow(17+index);
					list_row.add(row_detail);
					HSSFCell cell_workdetail_0 = row_detail.createCell((short)0);
					list_peopleCell.add(cell_workdetail_0);
					cell_workdetail_0.setCellValue(workvo.getBegindate()==null?str:workvo.getBegindate()+"");//��ʼʱ��
					sheet.addMergedRegion(new Region((short)(17+index), (short)0,(short)(17+index), (short)0));
					HSSFCell cell_workdetail_1 = row_detail.createCell((short)1);
					list_peopleCell.add(cell_workdetail_1);
					cell_workdetail_1.setCellValue(workvo.getEnddate()==null?str:workvo.getEnddate()+"");//����ʱ��
					sheet.addMergedRegion(new Region((short)(17+index), (short)1,(short)(17+index), (short)1));
					HSSFCell cell_workdetail_2 = row_detail.createCell((short)2);
					list_peopleCell.add(cell_workdetail_2);
					cell_workdetail_2.setCellValue(workvo.getWorkcorp()==null?str:workvo.getWorkcorp());//��λ����
					sheet.addMergedRegion(new Region((short)(17+index), (short)2,(short)(17+index), (short)4));
					HSSFCell cell_workdetail_5 = row_detail.createCell((short)5);
					list_peopleCell.add(cell_workdetail_5);
					cell_workdetail_5.setCellValue(workvo.getWorkjob()==null?str:workvo.getWorkjob());//����ְλ
					sheet.addMergedRegion(new Region((short)(17+index), (short)5,(short)(17+index), (short)5));
					HSSFCell cell_workdetail_6 = row_detail.createCell((short)6);
					list_peopleCell.add(cell_workdetail_6);
					cell_workdetail_6.setCellValue(workvo.getWorkduty()==null?str:workvo.getWorkduty());//��������
					sheet.addMergedRegion(new Region((short)(17+index), (short)6,(short)(17+index), (short)9));
				}
			}
			if(workdetail == 0){
				index = index +1;
				HSSFRow row_work_wu = sheet.createRow(17+index);
				list_row.add(row_work_wu);
				HSSFCell cell_work_wu = row_work_wu.createCell((short)0);
				list_peopleCell.add(cell_work_wu);
				cell_work_wu.setCellValue(str);//
				sheet.addMergedRegion(new Region((short)(17+index), (short)0,(short)(17+index), (short)9));
			}
		}
		/*********************************��������end*******************************/
		
		/*********************************��ͥ���begin*******************************/
		
		HSSFRow row_home = sheet.createRow(18+index);
		list_row.add(row_home);
		HSSFCell cell_home_h = row_home.createCell((short)0);
		sheet.addMergedRegion(new Region((short)(18+index), (short)0,(short)(18+index), (short)9));
		list_bodyHeadCell.add(cell_home_h);
		cell_home_h.setCellValue("�� Ҫ �� ͥ �� Ա");//
		cell_home_h.setCellStyle(bodyHeadStyle);
		
		HSSFRow row_home_b = sheet.createRow(19+index);
		list_row.add(row_home_b);
		HSSFCell cell_home_b0 = row_home_b.createCell((short)0);
		list_cell.add(cell_home_b0);
		cell_home_b0.setCellValue("����");//
		sheet.addMergedRegion(new Region((short)(19+index), (short)0,(short)(19+index), (short)0));
		HSSFCell cell_home_b1 = row_home_b.createCell((short)1);
		list_cell.add(cell_home_b1);
		cell_home_b1.setCellValue("�뱾�˹�ϵ");//
		sheet.addMergedRegion(new Region((short)(19+index), (short)1,(short)(19+index), (short)1));
		HSSFCell cell_home_b2 = row_home_b.createCell((short)2);
		list_cell.add(cell_home_b2);
		cell_home_b2.setCellValue("��������");//
		sheet.addMergedRegion(new Region((short)(19+index), (short)2,(short)(19+index), (short)3));
		HSSFCell cell_home_b4 = row_home_b.createCell((short)4);
		list_cell.add(cell_home_b4);
		cell_home_b4.setCellValue("������ò");//
		sheet.addMergedRegion(new Region((short)(19+index), (short)4,(short)(19+index), (short)4));
		HSSFCell cell_home_b5 = row_home_b.createCell((short)5);
		list_cell.add(cell_home_b5);
		cell_home_b5.setCellValue("������ѧϰ����λ");//
		sheet.addMergedRegion(new Region((short)(19+index), (short)5,(short)(19+index), (short)6));
		HSSFCell cell_home_b7 = row_home_b.createCell((short)7);
		list_cell.add(cell_home_b7);
		cell_home_b7.setCellValue("ְλ");//
		sheet.addMergedRegion(new Region((short)(19+index), (short)7,(short)(19+index), (short)9));	
		
		if(bvos.length>0){
			int workdetail =0;
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMFamilyVO".equals(classname)){
					index=index+1;
					workdetail =workdetail +1;
					RMFamilyVO familyvo = (RMFamilyVO) bvos[i];
					HSSFRow row_family_detail = sheet.createRow(19+index);
					list_row.add(row_family_detail);
					HSSFCell cell_familydetail_0 = row_family_detail.createCell((short)0);
					list_peopleCell.add(cell_familydetail_0);
					cell_familydetail_0.setCellValue(familyvo.getMem_name()==null?str:familyvo.getMem_name()+"");//����
					sheet.addMergedRegion(new Region((short)(19+index), (short)0,(short)(19+index), (short)0));
					HSSFCell cell_faimilydetail_1 = row_family_detail.createCell((short)1);
					list_peopleCell.add(cell_faimilydetail_1);
					cell_faimilydetail_1.setCellValue(familyvo.getMem_relation()==null?str:
						map_defdoc.get(familyvo.getMem_relation()).toString());//�뱾�˹�ϵ
					sheet.addMergedRegion(new Region((short)(19+index), (short)1,(short)(19+index), (short)1));
					HSSFCell cell_familydetail_2 = row_family_detail.createCell((short)2);
					list_peopleCell.add(cell_familydetail_2);
					cell_familydetail_2.setCellValue(familyvo.getMem_birthday()==null?str:familyvo.getMem_birthday().toString());//����
					sheet.addMergedRegion(new Region((short)(19+index), (short)2,(short)(19+index), (short)3));
					
					HSSFCell cell_familydetail_4 = row_family_detail.createCell((short)4);
					list_peopleCell.add(cell_familydetail_4);
					cell_familydetail_4.setCellValue(familyvo.getAttributeValue("glbdef1")==null?str:familyvo.getAttributeValue("glbdef1").toString());//������ò
					sheet.addMergedRegion(new Region((short)(19+index), (short)4,(short)(19+index), (short)4));
					
					HSSFCell cell_familydetail_5 = row_family_detail.createCell((short)5);
					list_peopleCell.add(cell_familydetail_5);
					cell_familydetail_5.setCellValue(familyvo.getMem_corp()==null?str:familyvo.getMem_corp());//������λ
					sheet.addMergedRegion(new Region((short)(19+index), (short)5,(short)(19+index), (short)6));
					
					HSSFCell cell_familydetail_7 = row_family_detail.createCell((short)7);
					list_peopleCell.add(cell_familydetail_7);
					cell_familydetail_7.setCellValue(familyvo.getMem_job()==null?str:familyvo.getMem_job());//ְλ
					sheet.addMergedRegion(new Region((short)(19+index), (short)7,(short)(19+index), (short)9));
				}
			}
			if(workdetail == 0){
				index = index+1;
				HSSFRow row_train_wu = sheet.createRow(19+index);
				list_row.add(row_train_wu);
				HSSFCell cell_train_wu = row_train_wu.createCell((short)0);
				list_peopleCell.add(cell_train_wu);
				cell_train_wu.setCellValue(str);//
				sheet.addMergedRegion(new Region((short)(19+index), (short)0,(short)(19+index), (short)9));
			}
		}
		/*********************************��ͥ���end*******************************/
		
	    /*********************************�������begin*******************************/
		HSSFRow row_enc = sheet.createRow(20+index);
		list_row.add(row_enc);
		HSSFCell cell_enc_h = row_enc.createCell((short)0);
		list_bodyHeadCell.add(cell_enc_h);
		cell_enc_h.setCellValue("��Ҫ�������");//
		sheet.addMergedRegion(new Region((short)(20+index), (short)0,(short)(20+index), (short)9));
		HSSFRow row_enc_b = sheet.createRow(21+index);
		list_row.add(row_enc_b);
		HSSFCell cell_enc_b0 = row_enc_b.createCell((short)0);
		list_cell.add(cell_enc_b0);
		cell_enc_b0.setCellValue("����ʱ��");//
		sheet.addMergedRegion(new Region((short)(21+index), (short)0,(short)(21+index), (short)0));
		
	/*	HSSFCell cell_enc_b1 = row_enc_b.createCell((short)1);
		list_cell.add(cell_enc_b1);
		cell_enc_b1.setCellValue("������Ŀ");//
		sheet.addMergedRegion(new Region((short)(21+index), (short)1,(short)(21+index), (short)5));
		HSSFCell cell_enc_b6 = row_enc_b.createCell((short)6);
		list_cell.add(cell_enc_b6);
		cell_enc_b6.setCellValue("�������/����");//
		sheet.addMergedRegion(new Region((short)(21+index), (short)6,(short)(21+index), (short)7));
		*/
		//��������Ŀ�ͻ������/�����ϲ�Ϊ��������
		HSSFCell cell_enc_b1 = row_enc_b.createCell((short)1);
		list_cell.add(cell_enc_b1);
		cell_enc_b1.setCellValue("��������");//
		sheet.addMergedRegion(new Region((short)(21+index), (short)1,(short)(21+index), (short)7));
		
		
		HSSFCell cell_enc_b8 = row_enc_b.createCell((short)8);
		list_cell.add(cell_enc_b8);
		cell_enc_b8.setCellValue("���ͼ��𣨹���/ʡ/��/У��");//
		sheet.addMergedRegion(new Region((short)(21+index), (short)8,(short)(21+index), (short)9));
		if(bvos.length>0){
			int workdetail =0;
			for(int i=0;i<bvos.length;i++){
				String classname = bvos[i].getClass().getName();
				if("nc.vo.rm.psndoc.RMEncVO".equals(classname)){
					index=index+1;
					workdetail =workdetail +1;
					RMEncVO encvo = (RMEncVO) bvos[i];
					HSSFRow row_enc_detail = sheet.createRow(21+index);
					list_row.add(row_enc_detail);
					HSSFCell cell_encdetail_0 = row_enc_detail.createCell((short)0);
					list_peopleCell.add(cell_encdetail_0);
					cell_encdetail_0.setCellValue(encvo.getVencourdate()==null?str:encvo.getVencourdate()+"");//����ʱ��
					sheet.addMergedRegion(new Region((short)(21+index), (short)0,(short)(21+index), (short)0));
					/*HSSFCell cell_encdetail_1 = row_enc_detail.createCell((short)1);
					list_peopleCell.add(cell_encdetail_1);
					cell_encdetail_1.setCellValue(encvo.getVencourtype()==null?str:encvo.getVencourtype().toString());//��������
					sheet.addMergedRegion(new Region((short)(21+index), (short)1,(short)(21+index), (short)5));
					HSSFCell cell_encdetail_6 = row_enc_detail.createCell((short)6);
					list_peopleCell.add(cell_encdetail_6);
					cell_encdetail_6.setCellValue(encvo.getVencourmeas()==null?str:encvo.getVencourmeas());
					sheet.addMergedRegion(new Region((short)(21+index), (short)6,(short)(21+index), (short)7));*/
					HSSFCell cell_encdetail_1 = row_enc_detail.createCell((short)1);
					list_peopleCell.add(cell_encdetail_1);
					cell_encdetail_1.setCellValue(encvo.getVencourmeas()==null?str:encvo.getVencourmeas());//��������
					sheet.addMergedRegion(new Region((short)(21+index), (short)1,(short)(21+index), (short)7));
								
					HSSFCell cell_encdetail_8 = row_enc_detail.createCell((short)8);
					list_peopleCell.add(cell_encdetail_8);
					cell_encdetail_8.setCellValue(encvo.getAttributeValue("encourrank")==null?str:map_defdoc.get(encvo.getAttributeValue("encourrank")).toString());
					sheet.addMergedRegion(new Region((short)(21+index), (short)8,(short)(21+index), (short)9));
				}
			}
			if(workdetail == 0){
				index = index +1;
				HSSFRow row_train_wu = sheet.createRow(21+index);
				list_row.add(row_train_wu);
				HSSFCell cell_train_wu = row_train_wu.createCell((short)0);
				list_peopleCell.add(cell_train_wu);
				cell_train_wu.setCellValue(str);//
				sheet.addMergedRegion(new Region((short)(21+index), (short)0,(short)(21+index), (short)9));
			}
		}
		/*********************************�������end*******************************/
		
		/*********************************��Ҫ����ҵ��start*******************************/
		HSSFRow row_yj = sheet.createRow(22+index);
		//list_row.add(row_yj);
		row_yj.setHeight((short)2000);
		HSSFCell cell_yj_h = row_yj.createCell((short)0);
		list_cell.add(cell_yj_h);
		cell_yj_h.setCellValue("��Ҫѧϰ���������ž������о��ɹ�");
		sheet.addMergedRegion(new Region((short)(22+index), (short)0,(short)(22+index), (short)0));
		HSSFCell cell_yj_hh = row_yj.createCell((short)1);
		//list_cell.add(cell_yj_hh);		
		cell_yj_hh.setCellValue(psnvo.getAttributeValue("glbdef26")==null?str:psnvo.getAttributeValue("glbdef26").toString());
		
		sheet.addMergedRegion(new Region((short)(22+index), (short)1,(short)(22+index), (short)9));
		
		/*********************************��Ҫ����ҵ��end*******************************/
		
		/*********************************��Ҫ����ҵ��start*******************************/
		HSSFRow row_zwpj = sheet.createRow(23+index);
		//list_row.add(row_zwpj);
		row_zwpj.setHeight((short)2000);
		HSSFCell cell_zwpj_h = row_zwpj.createCell((short)0);
		list_cell.add(cell_zwpj_h);
		cell_zwpj_h.setCellValue("��������");//
		sheet.addMergedRegion(new Region((short)(23+index), (short)0,(short)(23+index), (short)0));
		HSSFCell cell_zwpj_hh = row_zwpj.createCell((short)1);
		//list_cell.add(cell_zwpj_hh);		
		cell_zwpj_hh.setCellValue(psnvo.getEvaluation()==null?str:psnvo.getEvaluation());//
		
		sheet.addMergedRegion(new Region((short)(23+index), (short)1,(short)(23+index), (short)9));
		
		/*********************************��Ҫ����ҵ��end*******************************/
		
		/*********************************��������start*******************************/
		HSSFRow row_brsm = sheet.createRow(24+index);
		list_row.add(row_brsm);
		HSSFCell cell_brsm_h = row_brsm.createCell((short)0);
		list_cell.add(cell_brsm_h);
		cell_brsm_h.setCellValue("��������");//
		sheet.addMergedRegion(new Region((short)(24+index), (short)0,(short)(25+index), (short)0));
		HSSFCell cell_brsm_1 = row_brsm.createCell((short)1);
		list_cell.add(cell_brsm_1);
		cell_brsm_1.setCellValue("�Ա���ӦƸ���ύ����֤�顢���ϵ�ԭ���͸�ӡ������ȫ��ʵ��Ч�ģ���������������ʵ��������٣�����Ը��е���Ӧ�������Σ�������ӦƸ��λ�Ĵ���");//
		sheet.addMergedRegion(new Region((short)(24+index), (short)1,(short)(24+index), (short)9));
		HSSFRow row_brsm_2 = sheet.createRow(25+index);
		list_row.add(row_brsm_2);
		HSSFCell cell_brsm_2 = row_brsm_2.createCell((short)1);
		cell_brsm_2.setCellValue("����ǩ����                             ʱ��:       ��      ��       ��");		
		sheet.addMergedRegion(new Region((short)(25+index), (short)1,(short)(25+index), (short)9));
					
		/*********************************��������end*******************************/
		
		
		/*********************************��עstart*******************************/
		HSSFRow row_memo_1 = sheet.createRow(26+index);
		row_memo_1.setHeight((short)500);
		HSSFCell cell_memo_1 = row_memo_1.createCell((short)0);
		list_cell.add(cell_memo_1);
		cell_memo_1.setCellValue("ע�������ݽ϶�ɽ��������������Ҫ�ı�ԭ��Ԫ���п�û�е�������ޡ�,�ൺ�������Ź�����http://qd-metro.com");//
		setRegionStyle(sheet,new Region(26+index,(short)0,26+index,(short)9),bodyStyle);
		sheet.addMergedRegion(new Region((short)(26+index), (short)0,(short)(26+index), (short)9));
		
		/*********************************��ע����end*******************************/
		/***
		 * ���ñ���ϲ���Ԫ��߿�
		 * 
		 */
		for (int i = 2; i < sheet.getLastRowNum(); i++) {
			setRegionStyle(sheet,new Region(i,(short)0,i,(short)9),bodyStyle);
		}
		//�����и�
		setRowH(list_row);
		//���ñ��塢�����ͷ��Ԫ���ʽ
		setMutiCellStyle(workbook,list_cell,getBodyStyle(workbook));
		setMutiCellStyle(workbook,list_peopleCell,getBodyByPeopleStyle(workbook));
		cell_zwpj_hh.setCellStyle(getBodyByJLStyle(workbook));
		cell_yj_hh.setCellStyle(getBodyByJLStyle(workbook));
		setMutiCellStyle(workbook,list_bodyHeadCell,getBodyHeadStyle(workbook));
				
	}

	/****
	 * 
	 * ��������Ԫ���ʽ
	 * @param list_cell
	 * @param style
	 */
	private  void setMutiCellStyle(HSSFWorkbook workbook, List list_cell,HSSFCellStyle style){
				
		for(int i=0;i<list_cell.size();i++){
			((HSSFCell)list_cell.get(i)).setCellStyle(style);
		}
	}
	
	private  void setRowH(List list_row){
		for(int i=0;i<list_row.size();i++){
			((HSSFRow)list_row.get(i)).setHeight((short)500);
		}
	}
	
	/**
	* ���õ�Ԫ��߿򣨽���ϲ���Ԫ����ʾ���ֱ߿����⣩
	* @param sheet 
	* @param region
	* @param cs
	*/
	@SuppressWarnings("deprecation")
	public  void setRegionStyle(HSSFSheet sheet, Region region, HSSFCellStyle cs) {
	 for (int i = region.getRowFrom(); i <= region.getRowTo(); i++) {
	  HSSFRow row = HSSFCellUtil.getRow(i, sheet);
	  for (int j = region.getColumnFrom(); j <= region.getColumnTo(); j++) {
	   HSSFCell cell = HSSFCellUtil.getCell(row, (short) j);
	   cell.setCellStyle(cs);
	  }
	 }
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
		f_2.setFontHeightInPoints((short) 10.4);// �ֺ�
		style_2.setFont(f_2);
		style_2.setBorderBottom((short) 1);
		style_2.setBorderLeft((short) 1);
		style_2.setBorderRight((short) 1);
		style_2.setBorderTop((short) 1);
		for(int i=0;i<list_cell.size();i++){
			((HSSFCell)list_cell.get(i)).setCellStyle(style_2);
		}
		int lastrownum = workbook.getSheetAt(0).getLastRowNum();
		for(int i=0;i<lastrownum-1;i++){
			for(int j=0;j<12;j++){
				if(workbook.getSheetAt(0).getRow(i) != null){
					if(workbook.getSheetAt(0).getRow(i).getCell(j) == null){
						workbook.getSheetAt(0).getRow(i).createCell(j);
						workbook.getSheetAt(0).getRow(i).getCell(j).setCellStyle(style_2);
					}else{
						workbook.getSheetAt(0).getRow(i).getCell(j).setCellStyle(style_2);
					}
				}
			}
		}
	}
		
		
	/**
	 * ����ĳ��ͼƬ��ָ��������λ��
	 * @param HSSFWorkbook wb; Workbook����
	 * @param int sheetIndex; ����ͼƬ��sheet�±�����
	 * @param byte[] data; ͼƬ�ֽ���
	 * @param int startRow; ��ʼ��
	 * @param int startColumn; ��ʼ��
	 * @param int upRow; ������
	 * @param int upColumn; ������
	 * @param int index; ������������
	 */
	public static void insertImage(HSSFWorkbook wb, HSSFPatriarch patriarch,
			byte[] bytes, int startRow, int startColumn, int upRow, int upColumn,
			int index) {
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
		if (index != 0) {
			dx1 = index * 16;
			dy1 = index * 16;
			dx2 = 1024 - index * 16;
			dy2 = 256 - index * 16;		
		}
		HSSFClientAnchor anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2,
				(short) startColumn, startRow, (short) upColumn, upRow);
		anchor.setAnchorType(2);
		HSSFPicture hp=patriarch.createPicture(anchor, wb.addPicture(
				bytes, HSSFWorkbook.PICTURE_TYPE_JPEG));
		
	}	
	
	
	public CircularlyAccessibleValueObject[] getBvosBySort(CircularlyAccessibleValueObject[] bvos){
			int index=0;
			if(bvos.length>1){
				String classname = bvos[1].getClass().getName();
				if("nc.vo.rm.psndoc.RMEduVO".equals(classname)){	
					RMEduVO[] edus = (RMEduVO[]) bvos; 
					RMEduVO  edu = new RMEduVO();
					for(int i=0;i<edus.length;i++){
						for(int j=i+1;j<edus.length;j++){
							if(edus[i].getBegindate().after(edus[j].getBegindate())){
								edu = edus[i];
								edus[i] = edus[j];
								edus[j] = edu;								
							}
						}
					}
					bvos = edus;
				}				
		   }
			
			return bvos;
	}
}
