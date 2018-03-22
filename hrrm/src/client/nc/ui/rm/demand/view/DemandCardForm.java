 package nc.ui.rm.demand.view;
 
 import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.table.TableColumn;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.datatool.CommonUtils;
import nc.bs.hrss.pub.tool.CommonUtil;
import nc.bs.logging.Logger;
import nc.hr.utils.ResHelper;
import nc.itf.hrp.psnbudget.IDeptBudgetQueryService;
import nc.itf.rm.IDemandBudgetQueryService;
import nc.itf.rm.IDemandQueryService;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.hr.managescope.ref.MsDeptRefModel2;
import nc.ui.hr.uif2.view.HrBillFormEditor;
import nc.ui.ic.pub.util.CardPanelWrapper;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UITable;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.ui.pub.bill.BillCardBeforeEditListener;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillData;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillItemEvent;
import nc.ui.pub.bill.BillModel;
import nc.ui.pub.bill.BillScrollPane;
import nc.ui.pub.bill.BillScrollPane.BillTable;
import nc.ui.rm.demand.action.DemandEditLineAction;
import nc.ui.rm.demand.model.DemandAppModel;
import nc.ui.rm.pub.RMRefModelWherePartUtils;
import nc.ui.uif2.AppEvent;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.UIState;
import nc.ui.uif2.editor.value.BillCardPanelMetaDataValueAdapter;
import nc.ui.uif2.editor.value.IComponentValueStrategy;
import nc.ui.uif2.model.AbstractAppModel;
import nc.utils.iufo.CommitUtil;
import nc.vo.hr.managescope.ManagescopeBusiregionEnum;
import nc.vo.hrp.psndeptbudget.DeptBudgetVO;
import nc.vo.pub.BusinessException;
import nc.vo.uif2.LoginContext;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.lang.StringUtils;
 
 public class DemandCardForm extends HrBillFormEditor implements BillCardBeforeEditListener
 {	 
   IDemandQueryService  is =NCLocator.getInstance().lookup(IDemandQueryService.class);
   private DemandEditLineAction editLineAction;
   protected IComponentValueStrategy componentValueManager = new BillCardPanelMetaDataValueAdapter();
   public DemandCardForm() {}

   public Object getAllValue()
   {
     Object returnobject = null;
     
     getBillCardPanel().stopEditing();
     beforeGetValue();
     
 
     BillCardPanelMetaDataValueAdapter bdadapter = new BillCardPanelMetaDataValueAdapter();
     
     bdadapter.setComponent(this.billCardPanel);
     
     returnobject = ((BillCardPanel)bdadapter.getComponent()).getBillData().getBillObjectByMetaData();
     return returnobject;
   }
   
   public void initUI()
   {
     super.initUI();
     DemandTableCellRenderer cellRenderer = new DemandTableCellRenderer("rmnum");
     this.billCardPanel.getBodyPanel().getShowCol("rmnum").setCellRenderer(cellRenderer);
     getBillCardPanel().getBillTable().addMouseListener(new MouseAdapter()
     {
       private void doubleClick() {
         try {
           int selectedRow = DemandCardForm.this.getBillCardPanel().getBillTable().getSelectedRow();
           
           int oldnum = ((Integer)DemandCardForm.this.getBillCardPanel().getBillModel().getValueObjectAt(selectedRow, "rmnum")).intValue();
           DemandCardForm.this.getEditLineAction().doAction(null);
           int newnum = ((Integer)DemandCardForm.this.getBillCardPanel().getBillModel().getValueObjectAt(selectedRow, "rmnum")).intValue();
           if (newnum - oldnum != 0)
             DemandCardForm.this.syncBNNum();
         } catch (Exception e) {
           MessageDialog.showErrorDlg(DemandCardForm.this, null, e.getMessage());
           
           return;
         }
       }
       
       public void mouseClicked(MouseEvent arg0)
       {
         if ((arg0.getClickCount() == 2) && ((DemandCardForm.this.getModel().getUiState() == UIState.ADD) || (DemandCardForm.this.getModel().getUiState() == UIState.EDIT)))
         {
 
           doubleClick();
         }
       }
     });
     getBillCardPanel().setBillBeforeEditListenerHeadTail(this);
     syncBNNum();
     resetDept();
   }
   
   protected void onAdd()
   {
     super.onAdd();
     syncBudgetNum();
   }
   
   protected void onEdit()
   {
     super.onEdit();
     syncBudgetNum();
     
     if (((DemandAppModel)getModel()).isApproveSite()) {
       BillItem[] items = getBillCardPanel().getBillData().getHeadItems();
       for (BillItem item : items) {
         item.setEnabled(false);
         if ("remark".equals(item.getKey())) {
           item.setEnabled(true);
         }
       }
     }
   }
   

   private void syncBNNum()
   {
     try
     {
       int chz = getChildrenRMNum().intValue();
       
       Object objItem = getBillCardPanel().getHeadItem("pk_rmdept").getValueObject();
       String pk_dept = null;
       if (objItem == null)
         return;
       if ((objItem instanceof DefaultConstEnum)) {
         pk_dept = (String)((DefaultConstEnum)objItem).getValue();
       } else if ((objItem instanceof String)) {
         pk_dept = (String)objItem;
       }
       Integer budgetz = getDeptBudgetNum(pk_dept);
       int bnnum = 0;
       if (budgetz == null) {
         bnnum = chz;
       } else {
         bnnum = Math.min(chz, budgetz.intValue());
       }
       
       int bwnum = chz - bnnum;
       
       getBillCardPanel().getHeadItem("bnnum").setValue(Integer.valueOf(bnnum));
       getBillCardPanel().getHeadItem("bwnum").setValue(Integer.valueOf(bwnum));
       if (getBillCardPanel().getHeadItem("bnnum").getValueObject() != null)
       {
         getBillCardPanel().getHeadItem("bnnum").clearShowWaring();
       }
     } catch (Exception e) {
       Logger.error(e.getMessage());
     }
     syncBudgetNum();
   }

   private void syncBudgetNum()
   {
     if (!((DemandAppModel)getModel()).isHrpStarted()) {
       getBillCardPanel().getHeadItem("bnnum").setEnabled(false);
     }   
     Object bwnum = getBillCardPanel().getHeadItem("bwnum").getValueObject();
     
     if (((DemandAppModel)getModel()).isApproveSite()) {
       return;
     }
     Integer sysParam = Integer.valueOf((bwnum == null) || (((Integer)bwnum).intValue() == 0) ? ((DemandAppModel)getModel()).getBNParam() : ((DemandAppModel)getModel()).getBWParam());
     if (sysParam == null)
       sysParam = Integer.valueOf(0);
     boolean pkEditable = sysParam.intValue() == 1;

     getBillCardPanel().getHeadItem("pk_bustype").setEnabled(pkEditable);
     
     if (!pkEditable) {
       getBillCardPanel().getHeadItem("pk_bustype").setValue(null);
     }
   }
  
  //编辑后事件增加 编制提示    马鹏鹏
   public void afterEdit(BillEditEvent evt)
   {
     if ("pk_rmorg".equals(evt.getKey())) {
       getBillCardPanel().getHeadItem("pk_rmdept").setValue(null);
     }
     if ("pk_bustype".equals(evt.getKey()))
     {
       UIRefPane ref = (UIRefPane)getBillCardPanel().getHeadItem("pk_bustype").getComponent();       
       getBillCardPanel().setHeadItem("business_type", ref.getRefCode());
     }
     getBillCardPanel().getBodyPanel().getTable();
     int bodyRowCount = getBillCardPanel().getBodyPanel().getTable().getRowCount();
     if ("pk_rmdept".equals(evt.getKey())) {
       UIRefPane ref = (UIRefPane)getBillCardPanel().getHeadItem("pk_rmdept").getComponent();
       //获取组织
       String pk_deptorg = (String)ref.getRefValue("pk_father");
       //获取部门
       String pk_rmdept = getBillCardPanel().getHeadItem("pk_rmdept").getValue(); 
       getBillCardPanel().setHeadItem("pk_rmorg", pk_deptorg);            
      /**
       * 增加编辑后事件，
       * 如果是集团：查询出部门中层副职以下人员
       * 如果是分公司：查询出公司所有人员
       * 最后弹框提示缺编人数 
       */  
       //共同代码
       StringBuffer cmsb =new StringBuffer();
       cmsb.append("(select pk_dimension from ");
       cmsb.append("(select pk_dimension, dimension_year from hrp_dimension where pk_dim_doc =");
       cmsb.append("(select pk_dimension from hrp_dimension where pk_org =");
       cmsb.append("(select pk_org from org_adminorg where pk_adminorg = '"+pk_deptorg+"')");
       cmsb.append("and dimension_name = '职级编制维度') and dimension_state = 1");
       cmsb.append("order by dimension_year desc) where rownum = 1)");

       StringBuffer sb =null;
       //只有集团组织存在中层正职人员
       if("0001E410000000004QO4".equalsIgnoreCase(pk_deptorg)){
           //查询代码
           sb = new StringBuffer();
           sb.append("select budget_leftover from HRP_POSTBUDGET where pk_post =");
           sb.append("(select pk_dimensiondef from hrp_dimensiondef where pk_dimension ="); 
           sb.append(cmsb);
           sb.append("and pk_dimdefdoc = (select pk_dimensiondef from hrp_dimensiondef where dimensiondef_name = '中层副职以下人员编制'))");
           sb.append("and pk_dept_budget =");
           sb.append("(select pk_dept_budget from hrp_deptbudget where pk_dept = '"+pk_rmdept+"'");
           sb.append("and pk_dimension =");
           sb.append(cmsb);
           sb.append(")");	
       }else{
    	   //查询代码
           sb = new StringBuffer();
           sb.append("select budget_leftover from hrp_orgbudget_sub where pk_dimensiondef =");
           sb.append("(select pk_dimensiondef from hrp_dimensiondef where pk_dimension = "); 
           sb.append(cmsb);
           sb.append("and pk_dimdefdoc = (select pk_dimensiondef from hrp_dimensiondef where dimensiondef_name = '中层副职以下人员编制'))");
           sb.append("and pk_org_budget =");
           sb.append("(select pk_org_budget from hrp_orgbudget where pk_org ='"+pk_deptorg+"'");
           sb.append("and pk_dimension =");
           sb.append(cmsb);
           sb.append(")");	   	      	      	   
        } 
    	   String obj=null;
		try {			
			obj = is.getBudget(sb.toString());
		} catch (Exception e) {
			e.getStackTrace();
		}
       if(obj!=null&&!"查询编制异常".equalsIgnoreCase(obj)){
        	 MessageDialog.showHintDlg(this,"提示","本部门中层副职以下超（缺）编人数："+ obj);          		          	     	   
        }
     }         
       List<AbstractAction> actions = getTabActions();
       for (AbstractAction action : actions) {
         if ((action instanceof NCAction))
           ((NCAction)action).updateStatus();
       }
       if (bodyRowCount < 1) {
         return;
       }
       syncChildrenMes();
       syncBNNum();             
      
     if ("bnnum".equals(evt.getKey())) {
       Object bnitem = getBillCardPanel().getHeadItem("bnnum").getValueObject();
       if (bnitem == null) {
         ShowStatusBarMsgUtil.showErrorMsg("", ResHelper.getString("6021demand", "06021demand0033"), getModel().getContext());
         return;
       }
       int bwm = getChildrenRMNum().intValue() - ((Integer)bnitem).intValue();
       if (bwm < 0)
       {
         return;
       }
       getBillCardPanel().getHeadItem("bwnum").setValue(Integer.valueOf(bwm < 0 ? 0 : bwm));
       int initParam = bwm > 0 ? ((DemandAppModel)getModel()).getBWParam() : ((DemandAppModel)getModel()).getBNParam();
       boolean pkEditable = initParam == 1;
       
       getBillCardPanel().getHeadItem("pk_bustype").setEnabled(pkEditable);
       
       if (!pkEditable)
         getBillCardPanel().getHeadItem("pk_bustype").setValue(null);
     }
   }
   
   public DemandEditLineAction getEditLineAction() {
     return this.editLineAction;
   }
   
   public void setEditLineAction(DemandEditLineAction editLineAction) {
     this.editLineAction = editLineAction;
   }
   
   public boolean beforeEdit(BillItemEvent e)
   {
     if ("code".equals(((BillItem)e.getSource()).getKey()))
       return ((DemandAppModel)getModel()).isBillCodeEditable();
     if ("pk_rmorg".equals(((BillItem)e.getSource()).getKey())) {
       UIRefPane refPane = (UIRefPane)getBillCardPanel().getHeadItem("pk_rmorg").getComponent();
       
       refPane.getRefModel().setUseDataPower(false);
       refPane.getRefModel().addWherePart(RMRefModelWherePartUtils.getMsOrgWherePart(getModel().getContext().getPk_org()));
     }
     if ("pk_rmdept".equals(((BillItem)e.getSource()).getKey())) {
       UIRefPane ref = (UIRefPane)getBillCardPanel().getHeadItem("pk_rmdept").getComponent();
       MsDeptRefModel2 refmodel = (MsDeptRefModel2)ref.getRefModel();
       refmodel.setBusiregionEnum(ManagescopeBusiregionEnum.invite);
       refmodel.setPk_hrorg(getModel().getContext().getPk_org());
       String pk_rmorg = (String)getBillCardPanel().getHeadItem("pk_rmorg").getValueObject();
       refmodel.addWherePart(RMRefModelWherePartUtils.getMsDeptWherePart(pk_rmorg, getModel().getContext().getPk_org()));
       ref.setRefModel(refmodel);
     }
     if ("pk_bustype".equals(((BillItem)e.getSource()).getKey()))
     {
       ((UIRefPane)e.getItem().getComponent()).getRefModel().addWherePart(" and (( parentbilltype = '6803' and pk_group = '" + getModel().getContext().getPk_group() + "') or pk_billtypecode = '" + "6803" + "' )");
       
 
 
       ((UIRefPane)e.getItem().getComponent()).getRefModel().reloadData();
     }
     return true;
   }
   
   private void resetDept()
   {
     String sql = nc.vo.rm.pub.PubPermissionUtils.getDeptPermission();
     UIRefPane ref = (UIRefPane)this.billCardPanel.getHeadItem("pk_rmdept").getComponent();
     MsDeptRefModel2 refmodel = (MsDeptRefModel2)ref.getRefModel();
     refmodel.setBusiregionEnum(ManagescopeBusiregionEnum.invite);
     refmodel.setPk_hrorg(getModel().getContext().getPk_org());
     refmodel.addWherePart(" and enablestate = 2 and hrcanceled = 'N' " + sql);
     if (StringUtils.isEmpty(sql))
       return;
     ref.getRefModel().addWherePart(sql);
   }
   
 
 
 
   private Integer getDeptBudgetNum(String pk_dept)
     throws BusinessException
   {
     if (!((DemandAppModel)getModel()).isHrpStarted())
       return null;
     DeptBudgetVO deptBudgetVO = ((IDeptBudgetQueryService)NCLocator.getInstance().lookup(IDeptBudgetQueryService.class)).queryLastestDeptBudget(pk_dept);
     if (deptBudgetVO == null)
       return null;
    if (-deptBudgetVO.getBudget_leftover().intValue() < 0)
       return Integer.valueOf(0);
     return Integer.valueOf(-deptBudgetVO.getBudget_leftover().intValue());
   }
   
   private Integer getChildrenRMNum() {
     int num = 0;
     int count = getBillCardPanel().getBillTable().getRowCount();
     if (count == 0)
       return Integer.valueOf(num);
     for (int i = 0; i < count; i++) {
       int cnum = ((Integer)getBillCardPanel().getBodyValueAt(i, "rmnum")).intValue();
       num += cnum;
     }
     return Integer.valueOf(num);
   }
   
 
 
   private void syncChildrenMes()
   {
     Object objItem = getBillCardPanel().getHeadItem("pk_rmdept").getValueObject();
     String pk_dept = null;
     if (objItem == null)
       return;
     if ((objItem instanceof DefaultConstEnum)) {
       pk_dept = (String)((DefaultConstEnum)objItem).getValue();
     } else if ((objItem instanceof String))
       pk_dept = (String)objItem;
     int count = getBillCardPanel().getBodyPanel().getTable().getRowCount();
     if (count == 0)
       return;
     String pk_rmorg = (String)getBillCardPanel().getHeadItem("pk_rmorg").getValueObject();
     for (int i = 0; i < count; i++)
     {
       getBillCardPanel().setBodyValueAt(null, i, "pk_rmorg");
       getBillCardPanel().setBodyValueAt(null, i, "pk_rmdept");
       getBillCardPanel().setBodyValueAt(pk_rmorg, i, "pk_rmorg");
       getBillCardPanel().setBodyValueAt(pk_dept, i, "pk_rmdept");
       if ((getBillCardPanel().getBillModel().getRowState(i) == -1) || (getBillCardPanel().getBillModel().getRowState(i) == 0))
       {
         getBillCardPanel().getBillModel().setRowState(i, 2);
       }
     }
     getBillCardPanel().getBillData().getBillModel().loadLoadRelationItemValue();
   }
   
   public void handleEvent(AppEvent event) {
     super.handleEvent(event);
     if ("SyncNum".equals(event.getType())) {
       syncBNNum();
     }
   }
   public Object  getBudgetByAxis(String org,String dept) {
	   String url="http://172.16.19.31:8630/uapws/service/IDemandBudgetQueryService";
	   Service s = new Service();
	   Call call;
	   Object obj = null;
	try {
		call = (Call) s.createCall();
		call.setTargetEndpointAddress(new java.net.URL(url));
		call.setOperationName("getBudget");//请求方法
		call.addParameter(new QName(org),Constants.XSD_STRING, ParameterMode.IN);//添加参数
		call.addParameter(new QName(dept),Constants.XSD_STRING, ParameterMode.IN);
		call.setReturnClass(Object.class);//设置返回值		
		obj=call.invoke(new Object[]{org,dept});		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      return obj;	   	   
   }

 }