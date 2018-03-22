package nc.ui.rm.psndoc.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import nc.ui.hr.uif2.action.HrAction;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.rm.psndoc.model.RMPsndocAppModel;
import nc.ui.trade.business.HYPubBO_Client;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.RMPsndocVO;

/**
 * ������ѡͨ����ť
 * @author lichao
 *
 */
public class Dj2Cxtg extends HrAction {
	public static File _currentDirectoryPath = null;
	
	public Dj2Cxtg()
	  {
		  setCode("Dj2Cxtg");
		  String name = "�����ѡͨ����Ա";
		  setBtnName(name);
		  putValue("ShortDescription", name);
	  }

	@Override
	public void doAction(ActionEvent arg0) throws Exception {
		
		File file = addFile();
		if (file != null) {
			InputStream inputstream = new FileInputStream(file);
			HSSFWorkbook book = new HSSFWorkbook(inputstream);
			HSSFSheet sheet = null;
			int sheetNum = book.getNumberOfSheets();
			if (sheetNum == 0) {
				throw new Exception("Excel�ļ�����,����");
			}
			sheet = book.getSheetAt(0);
			int datalines = sheet.getLastRowNum();
			if(datalines<1){
				throw new Exception("Excel�ļ���û����Ч����,����");
			}
			StringBuffer sb = new StringBuffer();
			sb.append("  (");
			for (int j = 1; j <= datalines; j++) {
				if(getCell(getRow(sheet, j), (short) 0) != null && !"".equals(getCell(getRow(sheet, j), (short) 0))){
					String id = getCell(getRow(sheet, j), (short) 0).toString();
					sb.append("'"+id+"',");
				}
			}
			//RMPsnJobVO   
			RMPsnJobVO[] rmpsnvos = 
					(RMPsnJobVO[]) HYPubBO_Client.queryByCondition(RMPsnJobVO.class, 
							" pk_psndoc in (select c.pk_psndoc from rm_psndoc c where nvl(c.dr,0)=0 and c.id in "+sb.toString().substring(0, sb.toString().length()-1)+"))");
			if(rmpsnvos != null && rmpsnvos.length>0){
				for(int i=0;i<rmpsnvos.length;i++){
					rmpsnvos[i].setApplystatus(2);
//					RMPsndocVO psnvo = (RMPsndocVO) HYPubBO_Client.queryByPrimaryKey(RMPsndocVO.class, rmpsnvos[i].getPk_psndoc());
//					AggRMPsndocVO aggvo = new AggRMPsndocVO();
//					aggvo.setParentVO(psnvo);
//					((RMPsndocAppModel)getModel()).update(aggvo);
				}
				//ִ�и��²���
				HYPubBO_Client.updateAry(rmpsnvos);
				MessageDialog.showHintDlg(this.getEntranceUI(), "��ʾ", "������ɣ���ˢ�½��棡��");
//				List list = ((RMPsndocAppModel)getModel()).getData();
//				int index = list.size();
//				if(list != null && list.size()>0){
//					for(int i=0;i<index;i++){
//						AggRMPsndocVO aggvo = (AggRMPsndocVO) list.get(i);
//						((RMPsndocAppModel)getModel()).update(aggvo);
//					}
//				}
//				
			}
		}
		
	}
	/**
	 * ����ļ�����
	 * 
	 */
	private File addFile() throws Exception{
		File file = null;
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setCurrentDirectory(this.getCurrentDirectoryPath());
		ExcelFileFilter filter = new ExcelFileFilter();
		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this.getEntranceUI());
		if (returnVal == JFileChooser.CANCEL_OPTION) {
			throw new Exception("û��ѡ���ļ�");
		} else {
			_currentDirectoryPath = chooser.getSelectedFile();// �����ļ���ȡ·��
			file = chooser.getSelectedFile();

			if (!file.getPath().toLowerCase().endsWith(".xls")) {
				throw new Exception("��ѡ����ȷ�������ļ�");
			}
		}
		return file;
	}
	
	
	/**
	 * ������ļ���ȡ·��set()
	 * 
	 * @param currentDirectoryPath
	 */
	public void setCurrentDirectoryPath(File currentDirectoryPath) {
		_currentDirectoryPath = currentDirectoryPath;
	}
	
	/**
	 * ������ļ���ȡ·��get()
	 * 
	 * @return
	 */
	public File getCurrentDirectoryPath() {
		return _currentDirectoryPath;
	}
	
	/**
	 * Excel�ļ�������
	 * 
	 * @author jieely
	 * 
	 */
	class ExcelFileFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File file) {
			if (file.isDirectory() || file.getPath().toLowerCase().endsWith(".xls"))
				return true;
			else
				return false;
		}

		public String getDescription() {
			return "�����ļ�(.xls)";
		}
	}
	
	/**
	 * ��ȡsheet�е�HSSFCell����,���û�оʹ���һ��
	 * 
	 * @param row
	 *            HSSFCell�������ڵ���
	 * @param cellNum
	 *            ���ڵ��У������ӣ���ʼ
	 * @return
	 */
	private HSSFCell getCell(HSSFRow row, short cellNum) {
		HSSFCell cell = row.getCell(cellNum);
		return cell == null ? row.createCell(cellNum) : cell;
	}
	
	/**
	 * ��ȡsheet�е�HSSFRow����,���û�оʹ���һ��
	 * 
	 * @param sheet
	 * @param rowNum
	 *            ���ڵ��У������ӣ���ʼ
	 * @return
	 */
	private HSSFRow getRow(HSSFSheet sheet, int rowNum) {
		HSSFRow row = sheet.getRow(rowNum);
		return row == null ? sheet.createRow(rowNum) : row;
	}
}
