package nc.ui.rm.interview.view;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import nc.hr.utils.ResHelper;
import nc.ui.hr.frame.util.BillPanelUtils;
import nc.ui.hr.frame.util.table.TableMultiSelHelper;
import nc.ui.hr.uif2.view.HrBillListView;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UITable;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.ui.pub.beans.table.ColumnGroup;
import nc.ui.pub.beans.table.GroupableTableHeader;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillListData;
import nc.ui.pub.bill.BillListPanel;
import nc.ui.pub.bill.BillModel;
import nc.ui.pub.bill.BillScrollPane;
import nc.ui.pub.bill.BillTableCellRenderer;
import nc.ui.uif2.AppEvent;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.hr.tools.pub.GeneralVO;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.bill.BillTempletBodyVO;
import nc.vo.pub.bill.BillTempletVO;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.interview.InterviewPlanVO;
import nc.vo.rm.interview.InterviewVO;
import nc.vo.rm.psndoc.common.RMApplyTypeEnum;
import nc.vo.trade.voutils.VOUtil;
import nc.vo.uif2.LoginContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;


public abstract class InterviewBaseListView
  extends HrBillListView
{
  protected BillTempletBodyVO[] bodyVOs = null;
  protected int maxRound = 0;
  
  public InterviewBaseListView() {}
  
  public void initUI() { setSouth(createDescriptionPanel());
    super.initUI();
    this.billListPanel.setChildMultiSelect(false);
    this.bodyVOs = getBillListPanel().getBillListData().getBillTempletVO().getBodyVO();
    this.billListPanel.getHeadTable().setSortEnabled(false);
    getBillListPanel().getBodyUIPanel().setVisible(false);
    this.billListPanel.getHeadBillModel().setSortColumn(new String[] { "pk_reg_dept", "pk_psndoc_job" });
  }
  



  protected abstract JComponent createDescriptionPanel();
  


  public void initPageInfo()
  {
    int showOrder = 1000;
    BillTempletVO billTempletVO = getBillListPanel().getBillListData().getBillTempletVO();
    
    List<BillTempletBodyVO> itemList = new ArrayList();
    itemList.addAll(Arrays.asList(this.bodyVOs));
    BillTempletBodyVO bodvo = (BillTempletBodyVO)this.bodyVOs[0].clone();
    bodvo.setListshowflag(Boolean.valueOf(false));
    bodvo.setShowflag(Boolean.valueOf(false));
    bodvo.setDatatype(Integer.valueOf(0));
    bodvo.setDefaultshowname("hidden");
    bodvo.setItemkey("hidden");
    bodvo.setPos(Integer.valueOf(0));
    bodvo.setMetadatapath(null);
    bodvo.setMetadataproperty(null);
    bodvo.setList(true);
    bodvo.setListflag(Boolean.valueOf(true));
    bodvo.setShoworder(Integer.valueOf(showOrder++));
    bodvo.setWidth(Integer.valueOf(1));
    itemList.add(bodvo);
    String[] headInfo = getHeadRoundKey();
    
    for (int i = 0; i < this.maxRound; i++) {
      BillTempletBodyVO bodyvo = (BillTempletBodyVO)this.bodyVOs[0].clone();
      bodyvo.setListshowflag(Boolean.valueOf(true));
      bodyvo.setShowflag(Boolean.valueOf(true));
      bodyvo.setDatatype(Integer.valueOf(0));
      bodyvo.setDefaultshowname(getRoundNum(i + 1));
      bodyvo.setItemkey(headInfo[i]);
      bodyvo.setPos(Integer.valueOf(0));
      bodyvo.setMetadatapath(null);
      bodyvo.setMetadataproperty(null);
      bodyvo.setList(true);
      bodyvo.setListflag(Boolean.valueOf(true));
      bodyvo.setShoworder(Integer.valueOf(showOrder++));
      bodyvo.setWidth(Integer.valueOf(1));
      itemList.add(bodyvo);
    }
    
    remove(getBillListPanel());
    billTempletVO.setChildrenVO((CircularlyAccessibleValueObject[])itemList.toArray(new BillTempletBodyVO[0]));
    getBillListPanel().setListData(new BillListData(billTempletVO));  
    GroupableTableHeader header = (GroupableTableHeader)getBillListPanel().getHeadTable().getTableHeader();
   
    
    header.addColumnGroup(getColumnGroup(headInfo));
    add(getBillListPanel());
  }
  




  protected String[] getHeadRoundKey()
  {
    List<String> headList = new ArrayList();
    for (int i = 0; i < this.maxRound; i++)
      headList.add("result_sub_" + i);
    return CollectionUtils.isEmpty(headList) ? null : (String[])headList.toArray(new String[0]);
  }
  



  private void synMaxRoundFormModel()
  {
    this.maxRound = 0;
    Object[] objs = getModel().getData().toArray();
    if (ArrayUtils.isEmpty(objs))
      return;
    for (Object obj : objs) {
      InterviewPlanVO[] vos = ((AggInterviewVO)obj).getInterviewPlanVOs();
      if (!ArrayUtils.isEmpty(vos))
      {
        this.maxRound = Math.max(this.maxRound, vos.length);
      }
    }
  }
  


  public GeneralVO[] getShowValues()
  {
    Object[] objs = getModel().getData().toArray();
    if (ArrayUtils.isEmpty(objs))
      return null;
    List<GeneralVO> resultList = new ArrayList();
    List<String> strConApp = new ArrayList();
    for (Object obj : objs) {
      AggInterviewVO aggVO = (AggInterviewVO)obj;
      InterviewVO headVO = aggVO.getInterviewVO();
      GeneralVO vo = new GeneralVO();
      resultList.add(vo);
      
      String[] names = headVO.getAttributeNames();
      for (String name : names) {
        if (!name.equals("pk_psndoc_job"))
        {

          if (name.equals("pk_reg_dept")) {
            if (strConApp.contains(headVO.getPk_reg_dept() + headVO.getPk_reg_job())) {
              vo.setAttributeValue("hidden", "Y");
            } else
              vo.setAttributeValue("hidden", "N");
            strConApp.add(headVO.getPk_reg_dept() + headVO.getPk_reg_job());
            vo.setAttributeValue("pk_reg_dept", headVO.getPk_reg_dept());
            vo.setAttributeValue("pk_psndoc_job", headVO.getPk_psndoc_job());
          }
          else {
            vo.setAttributeValue(name, headVO.getAttributeValue(name));
          } }
      }
      //»•µÙ√Ê ‘¬÷¥Œ
//      InterviewPlanVO[] bodyvos = aggVO.getInterviewPlanVOs();
//      VOUtil.sort(bodyvos, new String[] { "roundnum" }, new int[] { 1 });
//      int bodyLength = ArrayUtils.getLength(bodyvos);
//      String[] headInfo = getHeadRoundKey();
//      for (int i = 0; i < headInfo.length; i++) {
//        vo.setAttributeValue(headInfo[i], i >= bodyLength ? Integer.valueOf(-1) : bodyvos[i].getResult());
//      }
    }
    return (GeneralVO[])resultList.toArray(new GeneralVO[0]);
  }
  
  protected ColumnGroup getColumnGroup(String[] headerName)
  {
    ColumnGroup columnGroup = new ColumnGroup(ResHelper.getString("6021interview", "06021interview0040"));
    
    if (ArrayUtils.isEmpty(headerName)) {
      return columnGroup;
    }
    for (int n = 0; n < headerName.length; n++) {
      TableColumn column = getBillListPanel().getParentListPanel().getShowCol(headerName[n]);
      columnGroup.add(column);
    }
    return columnGroup;
  }
  
  public String getRoundNum(int index) {
    switch (index) {
    case 1: 
      return ResHelper.getString("6021interview", "06021interview0041");
    case 2: 
      return ResHelper.getString("6021interview", "06021interview0042");
    case 3: 
      return ResHelper.getString("6021interview", "06021interview0043");
    case 4: 
      return ResHelper.getString("6021interview", "06021interview0044");
    case 5: 
      return ResHelper.getString("6021interview", "06021interview0045");
    case 6: 
      return ResHelper.getString("6021interview", "06021interview0046");
    case 7: 
      return ResHelper.getString("6021interview", "06021interview0047");
    case 8: 
      return ResHelper.getString("6021interview", "06021interview0048");
    case 9: 
      return ResHelper.getString("6021interview", "06021interview0049");
    case 10: 
      return ResHelper.getString("6021interview", "06021interview0050");
    }
    return null;
  }
  
  public void handleEvent(AppEvent event)
  {
    super.handleEvent(event);
    if ("Model_Initialized".equalsIgnoreCase(event.getType())) {
      synMaxRoundFormModel();
      initPageInfo();
      TableColumnModel columnModel = this.billListPanel.getHeadTable().getColumnModel();
      syncCellRender(columnModel);
      this.billListPanel.updateUI();
      BillPanelUtils.setPkorgToRefModel(getBillListPanel(), getModel().getContext().getPk_org());
      
      this.billListPanel.getHeadBillModel().setSortColumn(new String[] { "pk_reg_dept", "pk_psndoc_job", "pk_psndoc.applytype" });      
      this.billListPanel.getBodyBillModel().setSortColumn(new String[] { "roundnum" });
      getBillListPanel().getHeadBillModel().setBodyDataVO(getShowValues());
      this.billListPanel.getHeadTable().setSortEnabled(true);
      getBillListPanel().getHeadBillModel().loadLoadRelationItemValue();  
      getBillListPanel().setParentMultiSelect(true);
      setMultiSelectionMode(1);
      setListMultiProp();
     
    }
  }
  
  private void syncCellRender(TableColumnModel columnModel) {
    int regOrgIndex = this.billListPanel.getHeadBillModel().getBodyColByKey("pk_reg_org");
    regOrgIndex = this.billListPanel.getHeadTable().convertColumnIndexToView(regOrgIndex);
    IVBaseTableCellRenderer cellRender = new IVBaseTableCellRenderer();
    if (regOrgIndex >= 0) {
      TableColumn col = columnModel.getColumn(regOrgIndex);
      col.setCellRenderer(cellRender);
    }
    
    int regDeptIndex = this.billListPanel.getHeadBillModel().getBodyColByKey("pk_reg_dept");
    regDeptIndex = this.billListPanel.getHeadTable().convertColumnIndexToView(regDeptIndex);
    if (regDeptIndex >= 0) {
      TableColumn col = columnModel.getColumn(regDeptIndex);
      col.setCellRenderer(cellRender);
    }
    
    int regJobIndex = this.billListPanel.getHeadBillModel().getBodyColByKey("pk_psndoc_job.pk_reg_job");
    regJobIndex = this.billListPanel.getHeadTable().convertColumnIndexToView(regJobIndex);
    if (regJobIndex >= 0) {
      TableColumn col = columnModel.getColumn(regJobIndex);
      col.setCellRenderer(cellRender);
    }
    
    InterviewTableCellRenderer cellRenderer = new InterviewTableCellRenderer();
    cellRenderer.setModel(getModel());
    int roundIndex = this.billListPanel.getHeadBillModel().getBodyColByKey("result_sub_0");
    roundIndex = this.billListPanel.getHeadTable().convertColumnIndexToView(roundIndex);
    for (int i = roundIndex; (i >= 0) && (i < columnModel.getColumnCount()); i++) {
      columnModel.getColumn(i).setCellRenderer(cellRenderer);
    }
    
    int regPsnIndex = this.billListPanel.getHeadBillModel().getBodyColByKey("pk_psndoc");
    regPsnIndex = this.billListPanel.getHeadTable().convertColumnIndexToView(regPsnIndex);
    if (regPsnIndex >= 0) {
      TableColumn col = columnModel.getColumn(regPsnIndex);
      col.setCellRenderer(new BillTableCellRenderer()
      {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
          Component cmp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          BillModel billModel = (BillModel)table.getModel();
          DefaultConstEnum defaultEnum = (DefaultConstEnum)billModel.getValueObjectAt(row, "pk_psndoc.applytype");
          Integer typeValue = Integer.valueOf(defaultEnum == null ? 1 : ((Integer)defaultEnum.getValue()).intValue());
          if (RMApplyTypeEnum.INAPPLY.toIntValue() == typeValue.intValue())
            setBackground(Color.orange);
          return cmp;
        }
      });
    }
  }
}
