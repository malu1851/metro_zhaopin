package nc.ui.rm.psndoc.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import nc.bs.framework.common.NCLocator;
import nc.hr.utils.ResHelper;
import nc.itf.rm.IInterviewManageService;
import nc.itf.rm.IRMPsndocQueryService;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.ui.hr.uif2.action.HrAction;
import nc.ui.hr.uif2.view.HrBillListView;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.rm.psndoc.action.Dj2Cxtg.ExcelFileFilter;
import nc.ui.rm.psndoc.view.RMShowJobDialog;
import nc.ui.rm.pub.RMModelHelper;
import nc.ui.trade.business.HYPubBO_Client;
import nc.ui.uif2.IShowMsgConstant;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.pub.BusinessException;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsnJobVO;

public class Cxtg2msz extends HrAction {
	
public static File _currentDirectoryPath = null;
private BillManageModel interviewModel;
private HrBillListView listView;
	
	public Cxtg2msz()
	  {
		  setCode("Cxtg2msz");
		  String name = "导入面试中人员";
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
				throw new Exception("Excel文件有误,请检查");
			}
			sheet = book.getSheetAt(0);
			int datalines = sheet.getLastRowNum();
			if(datalines<1){
				throw new Exception("Excel文件中没有有效数据,请检查");
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
			String sql = "select c.pk_psndoc from rm_psndoc c where nvl(c.dr,0)=0 and c.id in"+
					sb.toString().substring(0, sb.toString().length()-1)+")";
			IUAPQueryBS bs = NCLocator.getInstance().lookup(IUAPQueryBS.class);
			List list = (List) bs.executeQuery(sql, new ArrayListProcessor());
			if(list != null && list.size()>0){
				String[] arg = new String[list.size()];
				for(int m=0;m<list.size();m++){
					Object[] obj = (Object[]) list.get(m);
					arg[m]=obj[0].toString();
				}
				IRMPsndocQueryService se = NCLocator.getInstance().lookup(IRMPsndocQueryService.class);
				AggRMPsndocVO[] aggvos = se.queryPsndocByPks(arg);
				if(aggvos != null && aggvos.length>0){
					for(int i=0;i<aggvos.length;i++){
						Apms(aggvos);
//						RMPsnJobVO[] psnjobvos = (RMPsnJobVO[])aggvos[i].getTableVO(RMPsnJobVO.getDefaultTableName());
//						if(psnjobvos != null && psnjobvos.length>0){
//							if(psnjobvos[0] != null){
//								psnjobvos[0].setApplystatus(3);
//								HYPubBO_Client.update(psnjobvos[0]);
//								//执行更新操作
//								((BillManageModel)getModel()).update(aggvos[0]);
//								MessageDialog.showHintDlg(this.getEntranceUI(), "提示", "更新完成");
//							}
//						}
					}
				}
			}
		}
	}
	
	
	  public void Apms(AggRMPsndocVO[] aggvos)throws Exception{
//		  Object[] aggvos = ((BillManageModel)getModel()).getSelectedOperaDatas();
		    if (ArrayUtils.isEmpty(aggvos)) {
		      throw new BusinessException(ResHelper.getString("6021psndoc", "06021psndoc0002"));
		    }
		    List<RMPsnJobVO> voList = new ArrayList();
		    for (Object selectData : aggvos)
		    {
		      AggRMPsndocVO aggVO = (AggRMPsndocVO)selectData;
		      if (!ArrayUtils.isEmpty(aggVO.getTableVO(RMPsnJobVO.getDefaultTableName()))) {
		        CollectionUtils.addAll(voList, aggVO.getTableVO(RMPsnJobVO.getDefaultTableName()));
		      }
		    }
		    if (CollectionUtils.isEmpty(voList)) {
		      throw new BusinessException(ResHelper.getString("6021psndoc", "06021psndoc0102", new String[] { getBtnName() }));
		    }
		    doArrange((RMPsnJobVO[]) aggvos[0].getTableVO(RMPsnJobVO.getDefaultTableName()),aggvos);
//		    RMShowJobDialog dlg = null;
//		    if ((aggvos.length == 1) && (voList.size() == 1))
//		    {
//		      if (1 != MessageDialog.showOkCancelDlg(getEntranceUI(), ResHelper.getString("6021pub", "06021pub0040", new String[] { getBtnName() }), ResHelper.getString("6021psndoc", "06021psndoc0003")))
//		      {
//		        putValue("message_after_action", ResHelper.getString("6001uif2", "06001uif20002"));
//		        return;
//		      }
//		      dlg = getDialog();
//		      dlg.doPrimary((RMPsnJobVO[])voList.toArray(new RMPsnJobVO[0]));
//		    }
//		    else
//		    {
//		      dlg = getDialog();
//		      dlg.setValue(voList.toArray(new RMPsnJobVO[0]));
//		      dlg.showModal();
//		    }
//		    clearDialog(dlg);
		    ShowStatusBarMsgUtil.showStatusBarMsg(IShowMsgConstant.getSaveSuccessInfo(), getContext());
	  }
			  
	  private RMShowJobDialog getDialog()
	  {
	    RMShowJobDialog dlg = new RMShowJobDialog(getEntranceUI())
	    {
	      public void doPrimary(RMPsnJobVO[] jobVOs)
	        throws Exception
	      {
//	    	  Cxtg2msz.this.doArrange(jobVOs);
	      }
	    };
	    dlg.setFromModel((BillManageModel)getModel());
	    dlg.setToModel(getInterviewModel());
	    dlg.setListView(getListView());
	    dlg.initUI();
	    return dlg;
	  }
	  
	  private void clearDialog(RMShowJobDialog dialog)
	  {
	    if (dialog == null) {
	      return;
	    }
	    dialog.setFromModel(null);
	    dialog.setToModel(null);
	    dialog.setListView(null);
	    dialog.dispose();
	    dialog = null;
	  }
	
	  private void doArrange(RMPsnJobVO[] jobVOs, AggRMPsndocVO[] selectDatas)throws Exception{
		  AggInterviewVO[] vos = 
				  ((IInterviewManageService)NCLocator.getInstance().lookup(IInterviewManageService.class)).
				  ArrangeInterviewByPsndoc(jobVOs);
//		  Object[] selectDatas = ((BillManageModel)getModel()).getSelectedOperaDatas();
		  List<AggRMPsndocVO> updateList = new ArrayList();
		    List<AggRMPsndocVO> deleteList = new ArrayList();
		    for (int i = 0; i < selectDatas.length; i++)
		    {
		      AggRMPsndocVO aggVO = (AggRMPsndocVO)selectDatas[i];
		      RMPsnJobVO[] subVOs = (RMPsnJobVO[])aggVO.getTableVO(RMPsnJobVO.getDefaultTableName());
		      for (RMPsnJobVO jobVO : jobVOs) {
		        if (ArrayUtils.contains(subVOs, jobVO)) {
		          subVOs = (RMPsnJobVO[])ArrayUtils.removeElement(subVOs, jobVO);
		        }
		      }
		      aggVO.setTableVO(RMPsnJobVO.getDefaultTableName(), subVOs);
		      if (ArrayUtils.isEmpty(subVOs)) {
		        deleteList.add(aggVO);
		      } else {
		        updateList.add(aggVO);
		      }
		    }
		    if (CollectionUtils.isNotEmpty(updateList)) {
		      ((BillManageModel)getModel()).directlyUpdate(updateList.toArray(new AggRMPsndocVO[0]));
		    }
		    if (CollectionUtils.isNotEmpty(deleteList)) {
		      ((BillManageModel)getModel()).directlyDelete(deleteList.toArray(new AggRMPsndocVO[0]));
		    }
		    if (!ArrayUtils.isEmpty(vos)) {
		      RMModelHelper.directMultiAdd(getInterviewModel(), vos);
		    }
	  }
			
	  public BillManageModel getInterviewModel()
	  {
	    return this.interviewModel;
	  }
	  
	  public void setInterviewModel(BillManageModel interviewModel)
	  {
	    this.interviewModel = interviewModel;
	  }
	  
	  public HrBillListView getListView()
	  {
	    return this.listView;
	  }
	  
	  public void setListView(HrBillListView listView)
	  {
	    this.listView = listView;
	  }
	  
	/**
	 * 添加文件方法
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
			throw new Exception("没有选择文件");
		} else {
			_currentDirectoryPath = chooser.getSelectedFile();// 保存文件读取路径
			file = chooser.getSelectedFile();

			if (!file.getPath().toLowerCase().endsWith(".xls")) {
				throw new Exception("请选择正确的数据文件");
			}
		}
		return file;
	}
	
	
	/**
	 * 最近的文件读取路径set()
	 * 
	 * @param currentDirectoryPath
	 */
	public void setCurrentDirectoryPath(File currentDirectoryPath) {
		_currentDirectoryPath = currentDirectoryPath;
	}
	
	/**
	 * 最近的文件读取路径get()
	 * 
	 * @return
	 */
	public File getCurrentDirectoryPath() {
		return _currentDirectoryPath;
	}
	
	/**
	 * Excel文件过滤类
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
			return "数据文件(.xls)";
		}
	}
	
	/**
	 * 获取sheet中的HSSFCell对象,如果没有就创建一个
	 * 
	 * @param row
	 *            HSSFCell对象所在的行
	 * @param cellNum
	 *            所在的列，索引从０开始
	 * @return
	 */
	private HSSFCell getCell(HSSFRow row, short cellNum) {
		HSSFCell cell = row.getCell(cellNum);
		return cell == null ? row.createCell(cellNum) : cell;
	}
	
	/**
	 * 获取sheet中的HSSFRow对象,如果没有就创建一个
	 * 
	 * @param sheet
	 * @param rowNum
	 *            所在的行，索引从０开始
	 * @return
	 */
	private HSSFRow getRow(HSSFSheet sheet, int rowNum) {
		HSSFRow row = sheet.getRow(rowNum);
		return row == null ? sheet.createRow(rowNum) : row;
	}
}
