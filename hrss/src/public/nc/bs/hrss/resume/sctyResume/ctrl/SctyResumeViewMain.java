package nc.bs.hrss.resume.sctyResume.ctrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nc.bs.hrss.pub.ServiceLocator;
import nc.bs.hrss.pub.cmd.AddCmd;
import nc.bs.hrss.pub.cmd.LineAddCmd;
import nc.bs.hrss.pub.cmd.LineDelCmd;
import nc.bs.hrss.pub.cmd.LineDownCmd;
import nc.bs.hrss.pub.cmd.LineInsertCmd;
import nc.bs.hrss.pub.cmd.LineUpCmd;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.pub.tool.SessionUtil;
import nc.bs.hrss.resume.schlResume.cmd.RmWebResumeSaveCmd;
import nc.bs.hrss.resume.schlResume.ctrl.SchlResumeViewMain;
import nc.bs.hrss.resume.schlResume.lsnr.SchlResumeAddProcessor;
import nc.itf.rm.IRMPsndocQueryService;
import nc.uap.lfw.core.AppInteractionUtil;
import nc.uap.lfw.core.bm.ButtonStateManager;
import nc.uap.lfw.core.cmd.CmdInvoker;
import nc.uap.lfw.core.cmd.UifEditCmdForAgg;
import nc.uap.lfw.core.comp.FormComp;
import nc.uap.lfw.core.comp.FormElement;
import nc.uap.lfw.core.comp.GridComp;
import nc.uap.lfw.core.comp.WebComponent;
import nc.uap.lfw.core.comp.WebElement;
import nc.uap.lfw.core.ctrl.IController;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.ctx.ViewContext;
import nc.uap.lfw.core.ctx.WindowContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.DatasetCellEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.model.plug.TranslatedRow;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.ViewComponents;
import nc.uap.lfw.core.page.ViewModels;
import nc.uap.lfw.core.serializer.impl.Dataset2SuperVOSerializer;
import nc.uap.lfw.core.serializer.impl.SuperVO2DatasetSerializer;
import nc.uap.lfw.jsp.uimeta.UIElement;
import nc.uap.lfw.jsp.uimeta.UILayoutPanel;
import nc.uap.lfw.jsp.uimeta.UIMeta;
import nc.uap.lfw.jsp.uimeta.UITabComp;
import nc.uap.lfw.jsp.uimeta.UITabItem;
import nc.vo.hrss.pub.SessionBean;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsndocVO;


public class SctyResumeViewMain
  implements IController
{
  private static final long serialVersionUID = 1L;
  
  public SctyResumeViewMain() {}
  
  public void pluginCatagoryPnl(Map<String, Object> keys)
  {
    TranslatedRow transRow = (TranslatedRow)keys.get("inTabId");
    String tabId = (String)transRow.getValue("param");
    ViewContext viewContext = AppLifeCycleContext.current().getWindowContext().getCurrentViewContext();
    UIMeta uiMeta = viewContext.getUIMeta();
    UITabComp tabComp = (UITabComp)uiMeta.findChildById("tag7165");
    List<UILayoutPanel> itemList = tabComp.getPanelList();
    for (int i = 0; i < itemList.size(); i++) {
      UITabItem tab = (UITabItem)itemList.get(i);
      if (!tab.getId().equals(tabId)) {
        tab.setVisible(false);
      }
      else
      {
        String datasetId = null;
        String componentId = tab.getElement().getId();
        WebComponent comp = getView().getViewComponents().getComponent(componentId);
        if ((comp instanceof FormComp)) {
          datasetId = ((FormComp)comp).getDataset();
        } else {
          datasetId = ((GridComp)comp).getDataset();
        }
        AppLifeCycleContext.current().getApplicationContext().addAppAttribute("detailDsId", datasetId);
        
        tab.setVisible(true);
        tabComp.setCurrentItem(Integer.toString(i));
        if (i == 0) {
          SchlResumeViewMain.setBtnLastState(false);
          SchlResumeViewMain.setBtnNextState(true);
          SchlResumeViewMain.setBtnSubmitState(true);
        } else if (i == itemList.size() - 1) {
          SchlResumeViewMain.setBtnLastState(true);
          SchlResumeViewMain.setBtnNextState(false);
          SchlResumeViewMain.setBtnSubmitState(true);
        } else {
          SchlResumeViewMain.setBtnLastState(true);
          SchlResumeViewMain.setBtnNextState(true);
          SchlResumeViewMain.setBtnSubmitState(true);
        }
      }
    }
    ButtonStateManager.updateButtons();
  }
  




  public void onDataLoad_dsBasicInfo(DataLoadEvent dataLoadEvent)
  {
    LfwView wdtMain = getView();
    
    setCatagoryPanel(0);
    
    SchlResumeViewMain.setBtnLastState(false);
    
    SchlResumeViewMain.setBtnSubmitState(true);
    
    SessionBean bean = SessionUtil.getRMWebSessionBean();
    RmUserVO rmUserVO = bean.getRmUserVO();
    if (rmUserVO == null) {
      SchlResumeViewMain.sendRedirectLogin("/app/RMWebLoginApp");
      return;
    }
    String pk_psndoc = rmUserVO.getHrrmpsndoc();
    if (StringUtil.isEmptyWithTrim(pk_psndoc)) {
      AddCmd addCmd = new AddCmd("dsBasicInfo", null, SchlResumeAddProcessor.class);
      CmdInvoker.invoke(addCmd);
    } else {
      UifEditCmdForAgg editCmdForAgg = new UifEditCmdForAgg("dsBasicInfo", pk_psndoc);
      CmdInvoker.invoke(editCmdForAgg);
      
      FormComp frmRMPsndoc = (FormComp)wdtMain.getViewComponents().getComponent("frmBasicInfo");
      frmRMPsndoc.getElementById("name").setEnabled(false);
      frmRMPsndoc.getElementById("idtype_name").setEnabled(false);
      frmRMPsndoc.getElementById("id").setEnabled(false);
      
      SchlResumeViewMain.setDetailDsEnabled(wdtMain);
    }
    
    SchlResumeViewMain.setAddrAddress(wdtMain);
  }
  
  public void onAfterDataChange(DatasetCellEvent datasetCellEvent) {
    Dataset ds = (Dataset)datasetCellEvent.getSource();
    int filedColIndex = datasetCellEvent.getColIndex();
    Row row = ds.getSelectedRow();
    int nameIndex = ds.nameToIndex("name");
    int idtypeIndex = ds.nameToIndex("idtype");
    int idIndex = ds.nameToIndex("id");
    int sexIndex = ds.nameToIndex("sex");
    AggRMPsndocVO aggVO = null;
    RMPsndocVO vo = null;
    
    if (("".equals(row.getValue(nameIndex))) || ("".equals(row.getValue(idtypeIndex))) || ("".equals(row.getValue(idIndex)))) {
      return;
    }
    if (((filedColIndex == nameIndex) || (filedColIndex == idtypeIndex) || (filedColIndex == idIndex) || (sexIndex == filedColIndex)) && 
      (ds.getValue(idIndex) != null) && (ds.getValue(idtypeIndex) != null) && (ds.getValue(nameIndex) != null)) {
      Dataset2SuperVOSerializer<RMPsndocVO> seri = new Dataset2SuperVOSerializer();
      RMPsndocVO voNew = ((RMPsndocVO[])seri.serialize(ds, row))[0];
      IRMPsndocQueryService service = null;
      try {
        service = (IRMPsndocQueryService)ServiceLocator.lookup(IRMPsndocQueryService.class);
        aggVO = service.queryPsndocByUniqueRule(voNew);
        if (aggVO != null) {
          vo = (RMPsndocVO)aggVO.getParentVO();
          if (vo != null) {
            SuperVO2DatasetSerializer serializer = new SuperVO2DatasetSerializer();
            if (sexIndex == filedColIndex) {
              vo.setSex((Integer)row.getValue(sexIndex));
            }
            serializer.vo2DataSet(vo, ds, ds.getSelectedRow());
            serializer.serialize((SuperVO[])aggVO.getTableVO("rm_psndoc_edu"), getView().getViewModels().getDataset("dsEduBkgrnd"));
            
            serializer.serialize((SuperVO[])aggVO.getTableVO("rm_psndoc_work"), getView().getViewModels().getDataset("dsJobinfo"));
            
            serializer.serialize((SuperVO[])aggVO.getTableVO("rm_psndoc_lagability"), getView().getViewModels().getDataset("dsLanguage"));
            
            serializer.serialize((SuperVO[])aggVO.getTableVO("rm_psndoc_train"), getView().getViewModels().getDataset("dsTrain"));
            
            serializer.serialize((SuperVO[])aggVO.getTableVO("rm_psndoc_project"), getView().getViewModels().getDataset("dsProject"));
            
            serializer.serialize((SuperVO[])aggVO.getTableVO("rm_psndoc_spe"), getView().getViewModels().getDataset("dsSkill"));
            
            serializer.serialize((SuperVO[])aggVO.getTableVO("rm_psndoc_enc"), getView().getViewModels().getDataset("dsReward"));
            
            serializer.serialize((SuperVO[])aggVO.getTableVO("rm_psndoc_family"), getView().getViewModels().getDataset("dsFamily"));
          }
        }
      }
      catch (HrssException e)
      {
        e.alert();
      } catch (BusinessException e) {
        new HrssException(e).deal();
      }
    }
  }
  






  public void setCatagoryPanel(int selectRow)
  {
    Dataset dataset = AppLifeCycleContext.current().getWindowContext().getViewContext("pv_hrss_catagory_selector").getView().getViewModels().getDataset("dsCatagory");
    
    dataset.setCurrentKey("MASTER_KEY");
    dataset.setRowSelectIndex(Integer.valueOf(selectRow));
  }
  




  public String getCurrentItem()
  {
    ViewContext viewContext = AppLifeCycleContext.current().getWindowContext().getCurrentViewContext();
    UIMeta uiMeta = viewContext.getUIMeta();
    UITabComp tabComp = (UITabComp)uiMeta.findChildById("tag7165");
    return tabComp.getCurrentItem();
  }
  





  public void doSubmit(MouseEvent mouseEvent)
  {
    List<String> notNullBodyList = new ArrayList();
    String[] detailDsIds = SchlResumeViewMain.getDetailDss();
    for (int i = 0; i < detailDsIds.length; i++) {
      Dataset ds = getView().getViewModels().getDataset(detailDsIds[i]);
      if (!ds.isNotNullBody())
        notNullBodyList.add(ds.getId());
    }
    boolean result=  AppInteractionUtil.showConfirmDialog("提示信息","承诺相关信息如不准确，本人承担所有责任！保存完成后不要忘了申请职位！");
    if(result){
    CmdInvoker.invoke(new RmWebResumeSaveCmd("dsBasicInfo",detailDsIds, AggRMPsndocVO.class.getName()));
    }
  }
  





  public void doNext(MouseEvent mouseEvent)
  {
    ViewContext viewContext = AppLifeCycleContext.current().getWindowContext().getCurrentViewContext();
    UIMeta uiMeta = viewContext.getUIMeta();
    UITabComp tabComp = (UITabComp)uiMeta.findChildById("tag7165");
    String currentItem = getCurrentItem();
    List<UILayoutPanel> itemList = tabComp.getPanelList();
    if (StringUtil.isEmptyWithTrim(currentItem)) {
      return;
    }
    if (itemList.size() < 1) {
      return;
    }
    for (int i = 0; i < itemList.size(); i++) {
      if (currentItem.equals(String.valueOf(i))) {
        if (currentItem.equals("0")) {
          tabComp.setCurrentItem("1");
          setCatagoryPanel(1);
          SchlResumeViewMain.setBtnLastState(true);
        } else if (currentItem.equals(String.valueOf(itemList.size() - 2))) {
          tabComp.setCurrentItem(String.valueOf(itemList.size() - 1));
          setCatagoryPanel(itemList.size() - 1);
          SchlResumeViewMain.setBtnNextState(false);
          SchlResumeViewMain.setBtnSubmitState(true);
        } else {
          tabComp.setCurrentItem(String.valueOf(i + 1));
          setCatagoryPanel(i + 1);
        }
      }
    }
    
    setTab(tabComp, tabComp.getCurrentItem());
    ButtonStateManager.updateButtons();
  }
  





  public void doLast(MouseEvent mouserEvent)
  {
    ViewContext viewContext = AppLifeCycleContext.current().getWindowContext().getCurrentViewContext();
    UIMeta uiMeta = viewContext.getUIMeta();
    UITabComp tabComp = (UITabComp)uiMeta.findChildById("tag7165");
    String currentItem = getCurrentItem();
    List<UILayoutPanel> itemList = tabComp.getPanelList();
    if (StringUtil.isEmptyWithTrim(currentItem)) {
      return;
    }
    
    for (int i = 0; i < itemList.size(); i++) {
      if (currentItem.equals(String.valueOf(i))) {
        if (currentItem.equals(String.valueOf(itemList.size() - 1))) {
          tabComp.setCurrentItem(String.valueOf(itemList.size() - 2));
          setCatagoryPanel(itemList.size() - 2);
          SchlResumeViewMain.setBtnLastState(true);
        } else if (currentItem.equals("1")) {
          tabComp.setCurrentItem("0");
          setCatagoryPanel(0);
          SchlResumeViewMain.setBtnLastState(false);
        } else {
          tabComp.setCurrentItem(String.valueOf(i - 1));
          setCatagoryPanel(i - 1);
        }
      }
    }
    
    setTab(tabComp, tabComp.getCurrentItem());
    ButtonStateManager.updateButtons();
  }
  





  public void setTab(UITabComp tabComp, String currentKey)
  {
    List<UILayoutPanel> itemList = tabComp.getPanelList();
    for (int i = 0; i < itemList.size(); i++) {
      UITabItem tab = (UITabItem)itemList.get(i);
      if (String.valueOf(i).equals(currentKey)) {
        tab.setVisible(true);
      }
      else {
        tab.setVisible(false);
      }
    }
  }
  
































  public void addIndi(MouseEvent mouseEvent)
  {
    String datasetId = SchlResumeViewMain.getDatasetId();
    if (StringUtil.isEmptyWithTrim(datasetId)) {
      return;
    }
    CmdInvoker.invoke(new LineAddCmd(datasetId, null));
  }
  




  public void deleteIndi(MouseEvent<WebElement> mouseEvent)
  {
    String datasetId = SchlResumeViewMain.getDatasetId();
    if (StringUtil.isEmptyWithTrim(datasetId)) {
      return;
    }
    CmdInvoker.invoke(new LineDelCmd(datasetId, null));
  }
  




  public void insertIndi(MouseEvent<WebElement> mouseEvent)
  {
    String datasetId = SchlResumeViewMain.getDatasetId();
    if (StringUtil.isEmptyWithTrim(datasetId)) {
      return;
    }
    CmdInvoker.invoke(new LineInsertCmd(datasetId, null));
  }
  





  public void moveUp(MouseEvent<WebElement> mouseEvent)
  {
    String datasetId = SchlResumeViewMain.getDatasetId();
    if (StringUtil.isEmptyWithTrim(datasetId)) {
      return;
    }
    CmdInvoker.invoke(new LineUpCmd(datasetId, null));
  }
  





  public void moveDown(MouseEvent mouseEvent)
  {
    String datasetId = SchlResumeViewMain.getDatasetId();
    if (StringUtil.isEmptyWithTrim(datasetId)) {
      return;
    }
    CmdInvoker.invoke(new LineDownCmd(datasetId, null));
  }
  




  private static LfwView getView()
  {
    return AppLifeCycleContext.current().getViewContext().getView();
  }
}
