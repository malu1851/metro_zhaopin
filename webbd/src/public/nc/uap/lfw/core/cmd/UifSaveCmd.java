package nc.uap.lfw.core.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.hrss.pub.tool.CommonUtil;
import nc.bs.logging.Logger;
import nc.hr.utils.CommonUtils;
import nc.md.innerservice.IMetaDataQueryService;
import nc.md.innerservice.MDQueryService;
import nc.md.model.IBusinessEntity;
import nc.md.model.MetaDataException;
import nc.uap.cpb.baseservice.IUifCpbService;
import nc.uap.cpb.log.CpLogger;
import nc.uap.cpb.persist.dao.PtBaseDAO;
import nc.uap.lfw.core.cache.LfwCacheManager;
import nc.uap.lfw.core.cmd.base.UifCommand;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.ctx.ViewContext;
import nc.uap.lfw.core.ctx.WindowContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.DatasetRelation;
import nc.uap.lfw.core.data.DatasetRelations;
import nc.uap.lfw.core.data.Field;
import nc.uap.lfw.core.data.FieldSet;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.core.exception.LfwValidateException;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.LfwWindow;
import nc.uap.lfw.core.page.ViewModels;
import nc.uap.lfw.core.serializer.impl.Dataset2SuperVOSerializer;
import nc.uap.lfw.core.serializer.impl.Datasets2AggVOSerializer;
import nc.uap.lfw.core.uif.delegator.DefaultDataValidator;
import nc.uap.lfw.core.uif.delegator.IDataValidator;
import nc.uap.lfw.core.vo.LfwExAggVO;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMEduVO;
import nc.vo.rm.psndoc.RMEncVO;
import nc.vo.rm.psndoc.RMFamilyVO;
import nc.vo.rm.psndoc.RMLagabilityVO;
import nc.vo.rm.psndoc.RMProjectVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.RMPsnWorkVO;
import nc.vo.rm.psndoc.RMTrainVO;
import nc.vo.trade.pub.IExAggVO;
import org.apache.commons.lang.StringUtils;
import uap.lfw.core.md.util.LfwMdUtil;
import uap.web.bd.pub.CpSqlTranslateUtil;





/***
 * 
 * 添加简历保存前校验
 * 
 * @author 马鹏鹏
 *
 */

public class UifSaveCmd
  extends UifCommand
{
  private static final String IFLOW_BIZ_ITF = "nc.itf.uap.pf.metadata.IFlowBizItf";
  private String masterDsId;
  private String[] detailDsIds;
  private String aggVoClazz;
  private boolean bodyNotNull;
  private List<String> notNullBodyList;
  private String billPk;
  private boolean isEditView = false;
  
  private String parentViewId;
  
  private String parentDsId;
  
  private String[] parentDetailDsIds;
  
  private Map<String, String> checkFieldMap;
  
  private AggregatedValueObject aggVo;
  
  private String editState;
  private static String ADD_OPER = "add_oper";
  private static String EDIT_OPER = "edit_oper";
  
  public AggregatedValueObject getAggVo() {
    return this.aggVo;
  }
  
  public void setAggVo(AggregatedValueObject aggVo) { this.aggVo = aggVo; }
  
  public String getBillPk() {
    return this.billPk;
  }
  
  public void setBillPk(String billPk) { this.billPk = billPk; }
  
  public List<String> getNotNullBodyList() {
    return this.notNullBodyList;
  }
  
  public void setNotNullBodyList(List<String> notNullBodyList) { this.notNullBodyList = notNullBodyList; }
  
  public UifSaveCmd(String masterDsId, String[] detailDsIds, String aggVoClazz, boolean bodyNotNull) {
    this.masterDsId = masterDsId;
    this.detailDsIds = detailDsIds;
    this.aggVoClazz = aggVoClazz;
    this.bodyNotNull = bodyNotNull;
  }
  
  public UifSaveCmd(String masterDsId2, String[] detailDsIds2, String aggVoClazz2) { this(masterDsId2, detailDsIds2, aggVoClazz2, true); }
  
  public UifSaveCmd() {}
  
  public void execute() {
    ViewContext viewCtx = getLifeCycleContext().getViewContext();
    LfwView widget = viewCtx.getView();
    if (widget == null)
      throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifSaveCmd-000000"));
    if (this.masterDsId == null)
      throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifSaveCmd-000001"));
    Dataset masterDs = widget.getViewModels().getDataset(this.masterDsId);
    if (masterDs == null)
      throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifMultiDelCmd-000001") + this.masterDsId + "!");
    if (this.aggVoClazz == null)
      this.aggVoClazz = LfwExAggVO.class.getName();
    List<String> idList = new ArrayList();
    idList.add(this.masterDsId);
    if ((this.detailDsIds != null) && (this.detailDsIds.length > 0))
      idList.addAll(Arrays.asList(this.detailDsIds));
    ArrayList<Dataset> detailDs = new ArrayList();
    if ((this.detailDsIds != null) && (this.detailDsIds.length > 0)) {
      for (int i = 0; i < this.detailDsIds.length; i++) {
        Dataset ds = widget.getViewModels().getDataset(this.detailDsIds[i]);
        if (ds != null)
          detailDs.add(ds);
      }
    }
    doValidate(masterDs, detailDs);
    Datasets2AggVOSerializer ser = new Datasets2AggVOSerializer();
    Dataset[] detailDss = (Dataset[])detailDs.toArray(new Dataset[0]);
    this.aggVo = getAggVO(masterDs, detailDss, this.aggVoClazz);
    if (this.aggVo == null) {
      return;
    }
    //校园简历保存前添加教育经历校验     马鹏鹏
    if(this.aggVo instanceof AggRMPsndocVO ){
    	AggRMPsndocVO aggRmVo = (AggRMPsndocVO) this.aggVo;             
        CircularlyAccessibleValueObject[] bvos=aggRmVo.getAllChildrenVO();
        int index = 0;
        int work_index = 0;
        int enc_index = 0;
       for(int i = 0;i<bvos.length;i++){
        	String classname = bvos[i].getClass().getName();
        	//教育经历页签 ：字符串去除空格、回车、换行符、制表符@author:柳衍志
        	if("nc.vo.rm.psndoc.RMEduVO".equals(classname.substring(0, classname.indexOf("$$")))){
        		index+=1;
        		nc.vo.rm.psndoc.RMEduVO eduvo= (RMEduVO) bvos[i];
        		eduvo.setSchool(trim(eduvo.getSchool()));
        		eduvo.setSchooltype(trim(eduvo.getSchooltype()));
        		eduvo.setMajor(trim(eduvo.getMajor()));
        		eduvo.setMajortype(trim(eduvo.getMajortype()));
        		eduvo.setStudymode(trim(eduvo.getStudymode()));
        		eduvo.setEducation(trim(eduvo.getEducation()));
        		eduvo.setEducationctifcode(trim(eduvo.getEducationctifcode()));
        		eduvo.setDegree(trim(eduvo.getDegree()));
        		eduvo.setDegreectifcode(trim(eduvo.getDegreectifcode()));
        		eduvo.setDegreeunit(trim(eduvo.getDegreeunit()));
        	//工作经历页签 ：字符串去除空格、回车、换行符、制表符@author:柳衍志
        	}else if("nc.vo.rm.psndoc.RMPsnWorkVO".equals(classname.substring(0, classname.indexOf("$$")))){
        		work_index+=1;
        		nc.vo.rm.psndoc.RMPsnWorkVO psnworkvo=  (RMPsnWorkVO) bvos[i];
            	psnworkvo.setWorkachive(trim(psnworkvo.getWorkachive()));
            	psnworkvo.setWorkaddr(trim(psnworkvo.getWorkaddr()));
            	psnworkvo.setWorkcorp(trim(psnworkvo.getWorkcorp()));
            	psnworkvo.setWorkdept(trim(psnworkvo.getWorkdept()));
            	psnworkvo.setWorkduty(trim(psnworkvo.getWorkduty()));
            	psnworkvo.setWorkjob(trim(psnworkvo.getWorkjob()));
            	psnworkvo.setRelphone(trim(psnworkvo.getRelphone()));
            	psnworkvo.setCertifier(trim(psnworkvo.getCertifier()));
            	psnworkvo.setCertiphone(trim(psnworkvo.getCertiphone()));
            	psnworkvo.setBackgroud(trim(psnworkvo.getBackgroud()));
            	psnworkvo.setDismismatter(trim(psnworkvo.getDismismatter()));
            	psnworkvo.setRemark(trim(psnworkvo.getRemark()));
            //培训经历页签 ：字符串去除空格、回车、换行符、制表符@author:柳衍志		
            }else if("nc.vo.rm.psndoc.RMTrainVO".equals(classname.substring(0, classname.indexOf("$$")))){
        		nc.vo.rm.psndoc.RMTrainVO trainkvo= (RMTrainVO) bvos[i];
        		trainkvo.setName(trim(trainkvo.getName()));
        		trainkvo.setContent(trim(trainkvo.getContent()));
        		trainkvo.setResult(trim(trainkvo.getResult()));
        		trainkvo.setRemark(trim(trainkvo.getRemark()));
        	//项目经历页签 ：字符串去除空格、回车、换行符、制表符@author:柳衍志
        	}else if("nc.vo.rm.psndoc.RMProjectVO".equals(classname.substring(0, classname.indexOf("$$")))){
        		nc.vo.rm.psndoc.RMProjectVO projectvo= (RMProjectVO) bvos[i];
        		projectvo.setName(trim(projectvo.getName()));
        		projectvo.setProjectdesc(trim(projectvo.getProjectdesc()));
        		projectvo.setPart(trim(projectvo.getPart()));
        		projectvo.setResponsedesc(trim(projectvo.getResponsedesc()));
        		projectvo.setRemark(trim(projectvo.getRemark()));
        	//语言能力页签 ：字符串去除空格、回车、换行符、制表符@author:柳衍志	
        	}else if("nc.vo.rm.psndoc.RMLagabilityVO".equals(classname.substring(0, classname.indexOf("$$")))){
        		nc.vo.rm.psndoc.RMLagabilityVO lagabilityvo= (RMLagabilityVO) bvos[i];
        		lagabilityvo.setCertcode(trim(lagabilityvo.getCertcode()));
        		lagabilityvo.setCertname(trim(lagabilityvo.getCertname()));
        		lagabilityvo.setClaglevel(trim(lagabilityvo.getClaglevel()));
        		lagabilityvo.setClagskill(trim(lagabilityvo.getClagskill()));
        		lagabilityvo.setClagsort(trim(lagabilityvo.getClagsort()));
        		lagabilityvo.setRemark(trim(lagabilityvo.getRemark()));
        	//家庭情况页签 ：字符串去除空格、回车、换行符、制表符@author:柳衍志	
          	}else if("nc.vo.rm.psndoc.RMFamilyVO".equals(classname.substring(0, classname.indexOf("$$")))){
   		        nc.vo.rm.psndoc.RMFamilyVO familyvo=(RMFamilyVO) bvos[i];
   		        familyvo.setMem_corp(trim(familyvo.getMem_corp()));
   		        familyvo.setMem_job(trim(familyvo.getMem_job()));
   		        familyvo.setMem_name(trim(familyvo.getMem_name()));
   		        familyvo.setMem_relation(trim(familyvo.getMem_relation()));
   		        familyvo.setVprofession(trim(familyvo.getVprofession()));
   		        familyvo.setVrelaaddr(trim(familyvo.getVrelaaddr()));
   		        familyvo.setVrelaphone(trim(familyvo.getVrelaphone()));
   		        familyvo.setRemark(trim(familyvo.getRemark()));
        	//奖励情况页签 ：字符串去除空格、回车、换行符、制表符@author:柳衍志
          	}else if("nc.vo.rm.psndoc.RMEncVO".equals(classname.substring(0, classname.indexOf("$$")))){
          		enc_index +=1;
          		nc.vo.rm.psndoc.RMEncVO encvo= (RMEncVO) bvos[i];
        		encvo.setVencourtype(trim(encvo.getVencourtype()));
        		encvo.setVencourmeas(trim(encvo.getVencourmeas()));
        		encvo.setVencourunit(trim(encvo.getVencourunit()));
        		encvo.setVencourmatter(trim(encvo.getVencourmatter()));
        	} 
        }
        if(index<2){
        	CommonUtil.showMessageDialog("教育经历需大于1条，请调整后保存");
        	return ;  	
        }
        if(work_index>3){
        	CommonUtil.showMessageDialog("实习或者工作经历需3条以内，请调整后保存");
        	return ;  
        }
        if(enc_index>5){
        	CommonUtil.showMessageDialog("奖惩情况需5条以内，请调整后保存");
        	return ;  
        }
    	    	
    }
       
    
    if (this.aggVo.getParentVO().getStatus() == 2) {
      this.editState = ADD_OPER;
    } else {
      this.editState = EDIT_OPER;
    }
    
    
    String[] removeSessCacheKeys = fillCachedDeletedVO(this.aggVo, detailDss);
    try {
      setBillStatus(masterDs, this.aggVo);
      
      setCheckField();
      
      onBeforeVOSave(this.aggVo);
      
      if (!checkBeforeVOSave(this.aggVo))
        return;
      
        
      onVoSave(this.aggVo);
    } catch (Exception e) {
      dealWithException(e);
    }
    
    setParentView();
    
    if (!this.isEditView) {
      onAfterVOSave(widget, masterDs, ser, detailDss, this.aggVo);
    } else {
      onUpdateParentView(ser, this.aggVo);
    }
    onAfterSave(masterDs, detailDss);
    removeSessCacheKeys(removeSessCacheKeys);
  }
  


  protected void setParentView() {}
  


  protected void checkDupliVO(SuperVO vo)
  {
    StringBuffer whereSql = new StringBuffer(" 1 = 2 ");
    StringBuffer message = new StringBuffer();
    
    if ((this.checkFieldMap != null) && (this.checkFieldMap.size() > 0)) {
      Iterator<Map.Entry<String, String>> it = this.checkFieldMap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, String> entry = (Map.Entry)it.next();
        
        String key = (String)entry.getKey();
        String value = (String)vo.getAttributeValue(key);
        whereSql.append(" OR ").append(key).append(" = '").append(CpSqlTranslateUtil.tmsql(value)).append("' ");
        message.append((String)entry.getValue());
        if (it.hasNext())
          message.append("/");
      }
      message.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifSaveCmd-000004"));
    }
    try
    {
      List<SuperVO> vos = (List)new PtBaseDAO().retrieveByClause(vo.getClass(), whereSql.toString());
      if ((vos != null) && (vos.size() > 0)) {
        for (SuperVO suervo : vos) {
          String pk = suervo.getPrimaryKey();
          if ((!StringUtils.isBlank(pk)) && (!pk.equals(vo.getPrimaryKey()))) {
            throw new LfwRuntimeException(message.toString());
          }
          
        }
        
      }
    }
    catch (DAOException e)
    {
      CpLogger.error(e.getMessage());
      throw new LfwRuntimeException(e.getMessage());
    }
  }
  
  protected void onUpdateParentView(Datasets2AggVOSerializer ser, AggregatedValueObject aggVo) {
    if (StringUtils.isBlank(this.parentViewId))
      throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifSaveCmd-000005"));
    if (StringUtils.isBlank(this.parentDsId))
      throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifSaveCmd-000006"));
    ViewContext parentViewCtx = getLifeCycleContext().getWindowContext().getViewContext(this.parentViewId);
    if (parentViewCtx == null)
      throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifEditOkClickCmd-000002"));
    LfwView parentWidget = parentViewCtx.getView();
    Dataset parentDs = parentWidget.getViewModels().getDataset(this.parentDsId);
    if (parentDs == null) {
      throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifSaveCmd-000007"));
    }
    











    onAfterVOSave(parentWidget, parentDs, ser, new Dataset[0], aggVo);
    
    getLifeCycleContext().getViewContext().getView().getPagemeta().setHasChanged(Boolean.valueOf(false));
    

    getLifeCycleContext().getApplicationContext().getCurrentWindowContext().closeView(AppLifeCycleContext.current().getViewContext().getId());
  }
  















  protected void setCheckField() {}
  















  protected void onAfterSave(Dataset masterDs, Dataset[] detailDss)
  {
    masterDs.setEnabled(false);
    for (int i = 0; i < detailDss.length; i++) {
      detailDss[i].setEnabled(false);
    }
    updateButtons();
  }
  



  protected void removeSessCacheKeys(String[] keys)
  {
    int len = keys != null ? keys.length : 0;
    for (int i = 0; i < len; i++) {
      if (keys[i] != null)
      {

        LfwCacheManager.getSessionCache().remove(keys[i]);
      }
    }
  }
  



  protected void dealWithException(Exception e)
  {
    Logger.error(e, e);
    if ((e instanceof LfwValidateException)) {
      throw ((LfwValidateException)e);
    }
    throw new LfwRuntimeException(e.getMessage());
  }
  







  protected void onAfterVOSave(LfwView widget, Dataset masterDs, Datasets2AggVOSerializer ser, Dataset[] detailDss, AggregatedValueObject aggVo)
  {
    if (detailDss == null) {
      DatasetRelations dsRels = widget.getViewModels().getDsrelations();
      if (dsRels != null) {
        DatasetRelation[] drs = dsRels.getDsRelations(masterDs.getId());
        if ((drs != null) && (drs.length > 0)) {
          detailDss = new Dataset[drs.length];
          for (int i = 0; i < drs.length; i++) {
            detailDss[i] = widget.getViewModels().getDataset(drs[i].getDetailDataset());
          }
        }
      }
    }
    if ((detailDss != null) && (detailDss.length > 0)) {
      DatasetRelations dsRels = widget.getViewModels().getDsrelations();
      if (dsRels != null) {
        DatasetRelation dr = dsRels.getDsRelation(masterDs.getId(), detailDss[0].getId());
        String newKeyValue = (String)aggVo.getParentVO().getAttributeValue(dr.getMasterKeyField());
        for (int i = 0; i < detailDss.length; i++) {
          if ((newKeyValue != null) && (!newKeyValue.equals(detailDss[i].getCurrentKey()))) {
            detailDss[i].replaceKeyValue(detailDss[i].getCurrentKey(), newKeyValue);
          }
        }
      }
    }
    ser.update(aggVo, masterDs, detailDss);
    
    String primaryKey = null;
    Field[] fields = masterDs.getFieldSet().getFields();
    for (int i = 0; i < fields.length; i++) {
      if (fields[i].isPrimaryKey()) {
        primaryKey = fields[i].getField();
        break;
      }
    }
    if (primaryKey != null) {
      String primaryValue = (String)aggVo.getParentVO().getAttributeValue(primaryKey);
      setBillPk(primaryValue);
    }
  }
  
  public String[] fillCachedDeletedVO(AggregatedValueObject aggVo, Dataset[] detailDss) {
    SuperVO masterVO = (SuperVO)aggVo.getParentVO();
    if (LfwCacheManager.getSessionCache() == null)
      return null;
    if (masterVO.getPrimaryKey() == null) {
      return null;
    }
    List<SuperVO> delBodyVoList = (List)LfwCacheManager.getSessionCache().get(masterVO.getPrimaryKey());
    if ((delBodyVoList == null) || (delBodyVoList.size() == 0)) {
      return null;
    }
    int len = detailDss != null ? detailDss.length : 0;
    
    String[] removeSessCacheKeys = new String[len + 1];
    
    for (int k = 0; k < len; k++) {
      Dataset dataset = detailDss[k];
      String delRowForeignKey = masterVO.getPrimaryKey() + "_" + dataset.getId();
      
      List<Row> listDelRow = (List)LfwCacheManager.getSessionCache().get(delRowForeignKey);
      if ((listDelRow != null) && (listDelRow.size() != 0))
      {
        Dataset2SuperVOSerializer ser = new Dataset2SuperVOSerializer();
        
        CircularlyAccessibleValueObject[] superVOs = ser.serialize(dataset, (Row[])listDelRow.toArray(new Row[0]));
        if ((aggVo instanceof IExAggVO)) {
          String tableId = null;
          Object tabcode = dataset.getExtendAttributeValue("$TAB_CODE");
          if (tabcode != null) {
            tableId = tabcode.toString();
          } else {
            Object parentField = dataset.getExtendAttributeValue("$PARENT_FIELD");
            if (parentField != null)
              tableId = parentField.toString();
          }
          if (tableId == null)
            tableId = dataset.getId();
          CircularlyAccessibleValueObject[] vos = ((IExAggVO)aggVo).getTableVO(tableId);
          List<CircularlyAccessibleValueObject> vosList = new ArrayList();
          for (CircularlyAccessibleValueObject vo : vos) {
            vosList.add(vo);
          }
          vosList.addAll(Arrays.asList(superVOs));
          
          ((IExAggVO)aggVo).setTableVO(tableId, (CircularlyAccessibleValueObject[])vosList.toArray(new SuperVO[0]));
        } else {
          CircularlyAccessibleValueObject[] vos = aggVo.getChildrenVO();
          List<CircularlyAccessibleValueObject> vosList = new ArrayList();
          for (int i = 0; i < vos.length; i++) {
            vosList.add(vos[i]);
          }
          vosList.addAll(Arrays.asList(superVOs));
          aggVo.setChildrenVO((CircularlyAccessibleValueObject[])vosList.toArray(new CircularlyAccessibleValueObject[0]));
        }
        removeSessCacheKeys[k] = delRowForeignKey;
      } }
    CircularlyAccessibleValueObject[] bodyVOs = null;
    if ((aggVo instanceof IExAggVO)) {
      bodyVOs = ((IExAggVO)aggVo).getAllChildrenVO();
    } else
      bodyVOs = aggVo.getChildrenVO();
    for (int i = 0; i < bodyVOs.length; i++) {
      SuperVO bodyVo = (SuperVO)bodyVOs[i];
      for (int j = 0; j < delBodyVoList.size(); j++) {
        SuperVO bodyVoChild = (SuperVO)delBodyVoList.get(j);
        if ((bodyVo.getPrimaryKey() != null) && (bodyVoChild.getPrimaryKey() != null) && (bodyVo.getPrimaryKey().equals(bodyVoChild.getPrimaryKey()))) {
          bodyVo.setStatus(3);
          break;
        }
      }
    }
    removeSessCacheKeys[len] = masterVO.getPrimaryKey();
    return removeSessCacheKeys;
  }
  
  protected void setBillStatus(Dataset masterDs, AggregatedValueObject aggVo) { Object metaObj = masterDs.getExtendAttributeValue("$META_ID");
    if (metaObj != null) {
      String metaId = metaObj.toString();
      try {
        IBusinessEntity entity = MDQueryService.lookupMDQueryService().getBusinessEntityByFullName(metaId);
        String billStateColumn = LfwMdUtil.getMdItfAttr(entity, "nc.itf.uap.pf.metadata.IFlowBizItf", "approvestate");
        if (billStateColumn != null) {
          Integer state = (Integer)aggVo.getParentVO().getAttributeValue(billStateColumn);
          if (state == null)
            aggVo.getParentVO().setAttributeValue(billStateColumn, Integer.valueOf(-1));
        }
      } catch (MetaDataException e) {
        Logger.error(e.getMessage(), e);
      }
    }
  }
  
  protected void doValidate(Dataset masterDs, List<Dataset> detailDs) throws LfwValidateException { ViewContext viewCtx = getLifeCycleContext().getViewContext();
    LfwView widget = viewCtx.getView();
    IDataValidator validator = getValidator();
    validator.validate(masterDs, widget);
    if (detailDs != null) {
      int size = detailDs.size();
      if (size > 0) {
        for (int i = 0; i < size; i++) {
          Dataset ds = (Dataset)detailDs.get(i);
          validator.validate(ds, widget);
          if ((this.notNullBodyList != null) && (this.notNullBodyList.contains(ds.getId()))) {
            doSingleValidateBodyNotNull(ds);
          }
        }
        if (this.bodyNotNull) {
          doValidateBodyNotNull(detailDs);
        }
      }
    }
  }
  



  protected void doSingleValidateBodyNotNull(Dataset detailDs)
    throws LfwValidateException
  {
    boolean hasBody = false;
    if (detailDs.getCurrentRowData() == null) {
      hasBody = false;
    }
    if (detailDs.getCurrentRowCount() > 0) {
      hasBody = true;
    }
    if (!hasBody)
      throw new LfwValidateException(detailDs.getCaption() + NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifSaveCmd-000002"));
  }
  
  protected void doValidateBodyNotNull(List<Dataset> detailDs) throws LfwValidateException {
    int size = detailDs.size();
    for (int i = 0; i < size; i++) {
      Dataset ds = (Dataset)detailDs.get(i);
      
      if ((ds.isNotNullBody()) && 
        (ds.getCurrentRowData() == null)) {
        throw new LfwValidateException(ds.getCaption() + NCLangRes4VoTransl.getNCLangRes().getStrByID("pub", "UifSaveCmd-000003"));
      }
    }
  }
  
  protected IDataValidator getValidator()
  {
    return new DefaultDataValidator();
  }
  




  protected void onBeforeVOSave(AggregatedValueObject aggVo)
  {
    checkDupliVO((SuperVO)aggVo.getParentVO());
  }
  





  protected boolean checkBeforeVOSave(AggregatedValueObject aggVo)
    throws Exception { return true; }
  
  protected void onVoSave(AggregatedValueObject aggVo) {
    try {
      IUifCpbService cpbService = (IUifCpbService)NCLocator.getInstance().lookup(IUifCpbService.class);
      cpbService.insertOrupdateAggVO(aggVo, isNotifyBDCache());
    }
    catch (BusinessException e) {
      Logger.error(e, e);
      throw new LfwRuntimeException(e.getMessage());
    }
  }
  
  public void setEditView(boolean isEditView) {
    this.isEditView = isEditView;
  }
  
  public boolean isEditView() { return this.isEditView; }
  
  public String getParentViewId() {
    return this.parentViewId;
  }
  
  public void setParentViewId(String parentViewId) { this.parentViewId = parentViewId; }
  
  public String getParentDsId() {
    return this.parentDsId;
  }
  
  public void setParentDsId(String parentDsId) { this.parentDsId = parentDsId; }
  
  public String[] getParentDetailDsIds() {
    return this.parentDetailDsIds;
  }
  
  public void setParentDetailDsIds(String[] parentDetailDsIds) { this.parentDetailDsIds = parentDetailDsIds; }
  

  public void setCheckFieldMap(Map<String, String> checkFieldMap) { this.checkFieldMap = checkFieldMap; }
  
  public Map<String, String> getCheckFieldMap() {
    if (this.checkFieldMap == null)
      this.checkFieldMap = new HashMap();
    return this.checkFieldMap;
  }
  
  protected AggregatedValueObject getAggVO(Dataset masterDs, Dataset[] detailDss, String aggVoClazz) {
    return new Datasets2AggVOSerializer().serialize(masterDs, detailDss, aggVoClazz);
  }
  /**
   * @author liuyanzhi
   * @date 2017-11-10
   * @param str
   * @return
   */
  public String trim(String str){
	  String returnstr="";
	  if(str!=null){
		Pattern pattern = Pattern.compile("\\s*|\t|\r|\n");  
        Matcher mathcher = pattern.matcher(str);
        returnstr=mathcher.replaceAll("");
	    }
	  return returnstr;
	  
  }
}
