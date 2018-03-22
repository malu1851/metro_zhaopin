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
import nc.ui.hr.uif2.view.HrBillListView;
import nc.ui.hrp.iomodel.action.GetDataAction;
import nc.ui.ic.pub.util.CardPanelWrapper;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillListPanel;
import nc.ui.pubapp.uif2app.view.ShowUpableBillForm;
import nc.ui.querytemplate.querytree.IQueryScheme;
import nc.ui.rm.interview.model.InterviewAppModel;
import nc.ui.rm.interview.view.InterviewListView;
import nc.ui.tb.zior.pluginaction.exportbatchexcel.ExcelFileFilter;
import nc.ui.trade.bill.BillCardPanelWrapper;
import nc.ui.trade.business.HYPubBO_Client;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.actions.RefreshAction;
import nc.ui.uif2.actions.StandAloneToftPanelActionContainer;
import nc.ui.uif2.model.BatchBillTableModel;
import nc.ui.uif2.model.BillManageModel;
import nc.ui.uif2.model.IQueryAndRefreshManager;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.interview.InterviewPlanVO;
import nc.vo.rm.interview.InterviewVO;

@SuppressWarnings("restriction")
public class GradeImporAction extends HrAction {
	

	IUAPQueryBS iUAPQueryBS = (IUAPQueryBS) NCLocator.getInstance().lookup(
			IUAPQueryBS.class.getName());

	private static final long serialVersionUID = 4606929888904487243L;
	
    private InterviewListView billLitView;
    
	public InterviewListView getBillListView() {
		return billLitView;
	}

	public void setBillListView(InterviewListView billListView) {
		this.billLitView= billListView;
	}

	public GradeImporAction() {

		setCode("GrageImport");
		setBtnName("����ɼ�");
	}

	/**
	 * ��ť�Ƿ����� ture===���� false===������
	 */

	protected boolean isActionEnable() {

		if (getModel().getSelectedData() == null) {
			return false;
		}
		AggInterviewVO aggvo = (AggInterviewVO) getModel().getSelectedData();
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
		
		Object[] invo = ((BillManageModel)getModel()).getSelectedOperaDatas();
		
		 if(invo==null){
		    	throw new BusinessException("��ѡ����Ա");
		    }

		int row = invo.length;
		/**
		 * 1����ȡ���ļ�ѡ�����Ի���ģ�͡������ġ���ڣ��ļ���ť==�ļ�ѡ������======������ļ�ѡ�����Ի���
		 * 
		 */
		File file = null;
		Workbook workbook = null;
		if (getUIFileChooser().showDialog(
				getModel().getContext().getEntranceUI(), "ѡ���ļ�") == JFileChooser.APPROVE_OPTION) {
			file = getUIFileChooser().getSelectedFile(); // �ļ�����ѡ����ѡ���ļ�
			if (file == null || file.getName().trim().length() == 0) { // ���ļ�Ϊ�ջ����ֳ���Ϊ0������ѡ���ļ���ʾ
				// ������Ϣ��ʾ����ʾ����
				MessageDialog.showErrorDlg(
						getModel().getContext().getEntranceUI(),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
								"ampub_0", "04501000-0382")/* @res "����" */,
						"��ѡ��Ҫ�����Excel�ļ�!");
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
            //BaseDAO dao = new BaseDAO();            
            //String s = (String)dao.executeQuery(sql_psndoc, new ColumnProcessor());
			String idcard = (String) iUAPQueryBS.executeQuery(sql_psndoc,
					new ColumnProcessor());

			// ���ļ� һ��sheetһ��sheet�ض�ȡ
			for (int numSheet = 0; numSheet < workbook.getNumberOfSheets(); numSheet++) {
				Sheet sheet = workbook.getSheetAt(numSheet);
				if (sheet == null) {
					continue;
				}
				int firstRowIndex = sheet.getFirstRowNum();
				int lastRowIndex = sheet.getLastRowNum();

				for (int rowIndex = firstRowIndex + 1; rowIndex <= lastRowIndex; rowIndex++) {

					Row currentRow = sheet.getRow(rowIndex);// ��ǰ��

					String id = currentRow.getCell((int) 1).toString();

					String wriscore = currentRow.getCell((int) 2).toString();

					String ivscore = currentRow.getCell((int) 3).toString();

					if (id.equals(idcard)) {

						ivvo.setWrittenscore(new UFDouble(wriscore));

						ivvo.setSocre(new UFDouble(ivscore));

						UFDouble aroundscore = ((new UFDouble(wriscore))
								.multiply(4).div(10))
								.add((new UFDouble(ivscore).multiply(6).div(10)));

						ivvo.setAllroundscore(new UFDouble(aroundscore));

						((BillManageModel) getModel()).update(aggvo);

						//HYPubBO_Client.update(ivvo);
					}

				}
			}
		}
	}

	/**
	 * �ļ�ѡ����
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
															 * "Microsoft Excel �ļ�"
															 */);
			fileChooser.setFileFilter(filter);
		}
		return fileChooser;
	}

}
