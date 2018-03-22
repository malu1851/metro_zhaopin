package nc.ui.rm.interview.action;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;

import nc.bs.framework.common.NCLocator;
import nc.hr.utils.ResHelper;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.rm.interview.model.InterviewBaseModel;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.lang.UFDate;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.interview.InterviewVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMEduVO;
import nc.vo.rm.psndoc.RMEncVO;
import nc.vo.rm.psndoc.RMFamilyVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.RMPsnWorkVO;
import nc.vo.rm.psndoc.RMPsndocVO;
import nc.vo.rm.psndoc.RMTrainVO;

/**
 * 导出面试成绩和体检结果
 * @author lichao  20170509
 *
 */
public class ExpInterViewExcel extends HrAction {

	
	  public ExpInterViewExcel()
	  {
	    setCode("ExpInterViewExcel");
	    String name = "导出成绩";
	    setBtnName(name);
	    putValue("ShortDescription", name);
	  }
	
	@SuppressWarnings("rawtypes")
	@Override
	public void doAction(ActionEvent arg0) throws Exception {
		Object[] selectDatas = ((InterviewBaseModel)getModel()).getSelectedOperaDatas();
	    if (ArrayUtils.isEmpty(selectDatas)) {
//	    	MessageDialog.showErrorDlg(this.getEntranceUI(), "错误", "请选择人员！");
	        throw new BusinessException("请选择人员！");
	    }else{
	    	
	    	JFileChooser chooser = new JFileChooser();
	    	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    	int RetVal = chooser.showOpenDialog(new JFrame());
	    	// 已经选择了文件  
	        if (RetVal == JFileChooser.APPROVE_OPTION){  
	            // 取得已选择文件的路径  
	        	String path = chooser.getSelectedFile().getPath();
		    	String date = (new UFDate()).getYear()+"-"+(new UFDate()).getMonth()+"-"+(new UFDate()).getDay();
//				String dirName = "d:" + File.separator +"面试人员成绩"+ File.separator +date;
				File file = new File(path);
				file.mkdirs();
		        BufferedWriter write = null;
		        try{
		        	write = new BufferedWriter(new FileWriter(path+File.separator+date+".xls")); 
		        }catch(Exception e){
		        	MessageDialog.showErrorDlg(this.getEntranceUI(),"错误",e.getMessage());
		        }
		        write.close();
				OutputStream o = new FileOutputStream(path+File.separator+date+".xls");
				HSSFWorkbook workbook = new HSSFWorkbook();
				HSSFSheet sheet = workbook.createSheet("面试人员成绩");
				//sheet.setDefaultRowHeight(rowHeight);
				
				HSSFCellStyle style = workbook.createCellStyle();
	    		HSSFFont f = workbook.createFont();
	    		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
	    		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	    		style.setVerticalAlignment((short)12);
	    		f.setFontHeightInPoints((short) 12);// 字号
	    		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// 下边框
	    		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
	    		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
	    		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
	    		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
	    		style.setWrapText(true);
	    		style.setFont(f);
				
				/******标题******/
	    		String[] title = {"应聘组织 ","应聘部门 ","应聘职位 ",
	    				"面试人","笔试成绩","面试成绩","综合成绩","体检结果","体检备注"};
	    		HSSFRow row_0 = sheet.createRow(0);
	    		for(int m=0;m<title.length;m++){
	    			HSSFCell cell = row_0.createCell((short)m);
	        		cell.setCellValue(title[m]);
	        		cell.setCellStyle(style);
	    		}
	    		sheet.setColumnWidth(0, 10000);
	    		sheet.setColumnWidth(1, 5000);
	    		sheet.setColumnWidth(2, 5000);
	    		sheet.setColumnWidth(3, 3000);
	    		sheet.setColumnWidth(4, 3000);
	    		sheet.setColumnWidth(5, 3000);
	    		sheet.setColumnWidth(6, 3000);
	    		sheet.setColumnWidth(7, 3000);
	    		sheet.setColumnWidth(8, 10000);
	    		
	    		f.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);// 正常
	    		style.setFont(f);
	    		
	    		StringBuffer sb = new StringBuffer();
	    		for(int i=0;i<selectDatas.length;i++){
	    			AggInterviewVO aggvo = (AggInterviewVO)selectDatas[i];
		    		InterviewVO itvo = aggvo.getInterviewVO();
		    		String pk_interview = itvo.getPk_interview();
		    		sb.append("'"+pk_interview+"',");
	    		}
	    		
	    		IUAPQueryBS bs = NCLocator.getInstance().lookup(IUAPQueryBS.class);
	    		StringBuffer sb_view = new StringBuffer();
	    		sb_view.append("select g.code,g.name,p.code,p.name,job.code,job.name,c.name,");
	    		sb_view.append("w.socre,w.writtenscore,w.allroundscore,w.testresult,w.remarks from rm_interview w  ");
	    		sb_view.append("left join org_adminorg g on w.pk_reg_org = g.pk_adminorg and nvl(g.dr,0)=0  ");
	    		sb_view.append("left join org_dept p on w.pk_reg_dept = p.pk_dept and nvl(p.dr,0)=0  ");
	    		sb_view.append("left join rm_psndoc_job b on w.pk_psndoc_job = b.pk_psndoc_job and nvl(b.dr,0)=0  ");
	    		sb_view.append("left join rm_publish ph on b.pk_reg_job = ph.pk_publishjob and nvl(ph.dr,0)=0  ");
	    		sb_view.append("left join rm_job job on ph.pk_job = job.pk_job and nvl(job.dr,0)=0  ");
	    		sb_view.append("inner join rm_psndoc c on w.pk_psndoc = c.pk_psndoc and nvl(c.dr,0)=0  ");
	    		sb_view.append("where w.pk_interview in (");
	    		sb_view.append(sb.substring(0, sb.length()-1)+" )");
	    		sb_view.append(" order by g.code,p.code,job.code,w.testresult desc,w.allroundscore,w.writtenscore desc,w.socre");
	    		List list_view = (List) bs.executeQuery(sb_view.toString(), new ArrayListProcessor());
	    		
		    	for(int i=0;i<list_view.size();i++){
		    		Object[] obj = (Object[]) list_view.get(i);
		    		Object[] values = {obj[1],obj[3],obj[5],
		    				obj[6],obj[7],obj[8],obj[9],
		    				obj[10],obj[11]};
		    		HSSFRow row_v = sheet.createRow(i+1);
		    		for(int n=0;n<values.length;n++){
		    			HSSFCell cell = row_v.createCell((short)n);
			    		cell.setCellValue(values[n]==null?"无":values[n].toString());
			    		cell.setCellStyle(style);
		    		}
		    	}
		    	workbook.write(o);
				o.flush();
				o.close();
				MessageDialog.showHintDlg(this.getEntranceUI(), "提示", "导出成绩成功！");
	        }  
	    }
	}
}
