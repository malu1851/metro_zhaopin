package nc.ui.rm.interview.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.ui.am.common.XlsFileFilter;
import nc.ui.cmp.applaybill.ClientUI;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.hrp.iomodel.action.GetDataAction;
import nc.ui.ic.pub.util.CardPanelWrapper;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pubapp.uif2app.view.ShowUpableBillForm;
import nc.ui.querytemplate.querytree.IQueryScheme;
import nc.ui.rm.interview.model.InterviewAppModel;
import nc.ui.rm.interview.model.PassInterviewAppModel;
import nc.ui.rm.pub.RMModelHelper;
import nc.ui.tb.zior.pluginaction.exportbatchexcel.ExcelFileFilter;
import nc.ui.trade.business.HYPubBO_Client;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.actions.RefreshAction;
import nc.ui.uif2.actions.StandAloneToftPanelActionContainer;
import nc.ui.uif2.model.BatchBillTableModel;
import nc.ui.uif2.model.BillManageModel;
import nc.ui.uif2.model.IQueryAndRefreshManager;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.interview.InterviewPlanVO;
import nc.vo.rm.interview.InterviewVO;

@SuppressWarnings("restriction")
public class ResultImportAction extends HrAction {

	IUAPQueryBS iUAPQueryBS = (IUAPQueryBS) NCLocator.getInstance().lookup(
			IUAPQueryBS.class.getName());

	
	private PassInterviewAppModel passivmodel;

	public ResultImportAction() {

		setCode("ResultImport");
		setBtnName("导入体检结果");
	}

	/**
	 * 按钮是否启用 ture===启用 false===不启用
	 */

	protected boolean isActionEnable() {

		if (getModel().getSelectedData() == null) {
		      return false;
		    }
		    AggInterviewVO aggvo = (AggInterviewVO)getModel().getSelectedData();
		    if (aggvo == null) {
		      return false;
		    }
		    if (aggvo.getInterviewVO() == null) {
		      return false;
		    }
		    return super.isActionEnable();
	

	}

	@Override
	public void doAction(ActionEvent e) throws Exception {

		Object[] invo = ((BillManageModel) getModel()).getSelectedOperaDatas();

		 if(invo==null){
		    	throw new BusinessException("请选择人员");
		    }

		int row = invo.length;

		/**
		 * 1、获取的文件选择器对话框（模型、上下文、入口，文件按钮==文件选择器）======导入的文件选择器对话框
		 * 
		 */
		File file = null;
		Workbook workbook = null;
		if (getUIFileChooser().showDialog(
				getModel().getContext().getEntranceUI(), "选择文件") == JFileChooser.APPROVE_OPTION) {
			file = getUIFileChooser().getSelectedFile(); // 文件查找选择器选择文件
			if (file == null || file.getName().trim().length() == 0) { // 若文件为空或名字长度为0，做出选择文件提示
				// 错误消息提示框，提示口令
				MessageDialog.showErrorDlg(
						getModel().getContext().getEntranceUI(),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
								"ampub_0", "04501000-0382")/* @res "错误" */,
						"请选择要导入的Excel文件!");
				return;
			}
			InputStream is = new FileInputStream(file);
			if (file.getPath().endsWith(".xls")) {
				workbook = new HSSFWorkbook(is);
			} else if (file.getPath().endsWith(".xlsx")) {
				workbook = new XSSFWorkbook(is);
			}
		} else {

			return;
		}

		for (int i = 0; i < row; i++) {

			AggInterviewVO aggvo = ((AggInterviewVO) invo[i]);

			InterviewVO ivvo = aggvo.getInterviewVO();

			String pk_psndoc = ((AggInterviewVO) invo[i]).getInterviewVO()
					.getPk_psndoc();

			String sql_psndoc = "select id from rm_psndoc  where pk_psndoc = '"
					+ pk_psndoc + "'";

			String idcard = (String) iUAPQueryBS.executeQuery(sql_psndoc,
					new ColumnProcessor());

			// 读文件 一个sheet一个sheet地读取
			for (int numSheet = 0; numSheet < workbook.getNumberOfSheets(); numSheet++) {
				Sheet sheet = workbook.getSheetAt(numSheet);
				if (sheet == null) {
					continue;
				}
				int firstRowIndex = sheet.getFirstRowNum();
				int lastRowIndex = sheet.getLastRowNum();

				for (int rowIndex = firstRowIndex + 1; rowIndex <= lastRowIndex; rowIndex++) {

					Row currentRow = sheet.getRow(rowIndex);// 当前行

					String id = currentRow.getCell(1).toString();

					String result = currentRow.getCell(2).toString();

					if (id.equals(idcard)) {

						if (result.equals("合格")) {

							ivvo.setTestresult("0");

						}
						if (result.equals("不合格")) {

							ivvo.setTestresult("1");

						}
						((BillManageModel) getModel()).update(aggvo);

						// HYPubBO_Client.update(ivvo);

					}

				}
			}
		}
	}

	/**
	 * 文件选择器
	 */
	private JFileChooser fileChooser = null;

	private JFileChooser getUIFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			XlsFileFilter filter = new XlsFileFilter();
			filter.addExtension("xlsx");
			filter.addExtension("xls");
			filter.setDescription(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("ampub_0", "04501000-0468")/*
															 * @res
															 * "Microsoft Excel 文件"
															 */);
			fileChooser.setFileFilter(filter);
		}
		return fileChooser;
	}
}
