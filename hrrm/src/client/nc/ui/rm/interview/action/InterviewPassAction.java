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
public class InterviewPassAction extends HrAction {

	IUAPQueryBS iUAPQueryBS = (IUAPQueryBS) NCLocator.getInstance().lookup(
			IUAPQueryBS.class.getName());

	private static final long serialVersionUID = 4606929888904487243L;
	
	private PassInterviewAppModel passivmodel;

	public InterviewPassAction() {

		setCode("GrageImport");
		setBtnName("面试通过");
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

		for (int i = 0; i < row; i++) {

			AggInterviewVO aggvo = ((AggInterviewVO) invo[i]);
			
			InterviewVO ivvo = aggvo.getInterviewVO();
			
			//设置通过状态
			ivvo.setInterviewstate(3);
			
			aggvo.setParentVO(ivvo);
			
			((BillManageModel) getModel()).update(aggvo);
			
			((InterviewAppModel)getModel()).directlyDelete(aggvo);
			
			RMModelHelper.directMultiAdd(getPassivmodel(), new Object[] { aggvo });

		
		}
	}

	public PassInterviewAppModel getPassivmodel() {
		return passivmodel;
	}

	public void setPassivmodel(PassInterviewAppModel passivmodel) {
		this.passivmodel = passivmodel;
	}
   

}
