package nc.impl.rm.publish;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nc.bs.framework.common.NCLocator;
import nc.hr.frame.persistence.SimpleDocServiceTemplate;
import nc.hr.utils.CommonUtils;
import nc.hr.utils.FromWhereSQLUtils;
import nc.hr.utils.InSQLCreator;
import nc.hr.utils.MultiLangHelper;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.hr.utils.StringPiecer;
import nc.itf.bd.defdoc.IDefdocQryService;
import nc.itf.org.IOrgUnitQryService;
import nc.itf.rm.IActiveQueryMaintain;
import nc.itf.rm.IPublishQueryService;
import nc.itf.rm.IRMJobQueryService;
import nc.ui.querytemplate.querytree.FromWhereSQL;
import nc.vo.bd.defdoc.DefdocVO;
import nc.vo.bd.region.RegionVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.rm.active.ActiveJobVO;
import nc.vo.rm.active.AggActiveVO;
import nc.vo.rm.job.AggRMJobVO;
import nc.vo.rm.job.RMJobVO;
import nc.vo.rm.publish.AggPublishVO;
import nc.vo.rm.publish.PublishJobVO;
import nc.vo.rm.publish.PublishStatusEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class PublishQueryServiceImpl
  implements IPublishQueryService
{
  private SimpleDocServiceTemplate serviceTemplate;
  private IRMJobQueryService rmJobQuery;
  private IOrgUnitQryService orgQuery;
  private IActiveQueryMaintain activeQuery;
  private IDefdocQryService defdocQueryService;
  
  public PublishQueryServiceImpl() {}
  
  private SimpleDocServiceTemplate getServiceTemplate()
  {
    if (this.serviceTemplate == null) {
      this.serviceTemplate = new SimpleDocServiceTemplate("1f993e1e-e198-41ef-9716-946e0c9103e6");
    }
    return this.serviceTemplate;
  }
  
  public IRMJobQueryService getRmJobQuery() {
    if (this.rmJobQuery == null)
      this.rmJobQuery = ((IRMJobQueryService)NCLocator.getInstance().lookup(IRMJobQueryService.class));
    return this.rmJobQuery;
  }
  
  public IOrgUnitQryService getOrgQuery() {
    if (this.orgQuery == null)
      this.orgQuery = ((IOrgUnitQryService)NCLocator.getInstance().lookup(IOrgUnitQryService.class));
    return this.orgQuery;
  }
  
  public IActiveQueryMaintain getActiveQuery() {
    if (this.activeQuery == null)
      this.activeQuery = ((IActiveQueryMaintain)NCLocator.getInstance().lookup(IActiveQueryMaintain.class));
    return this.activeQuery;
  }
  
  public IDefdocQryService getDefdocQueryService() {
    if (this.defdocQueryService == null) {
      this.defdocQueryService = ((IDefdocQryService)NCLocator.getInstance().lookup(IDefdocQryService.class.getName()));
    }
    return this.defdocQueryService;
  }
  

  public PublishJobVO[] ensureDetailInfos(PublishJobVO[] vos)
    throws BusinessException
  {
    if (ArrayUtils.isEmpty(vos))
      return null;
    Set<String> jobSet = new HashSet();
    Set<String> orgSet = new HashSet();
    Set<String> defdocSet = new HashSet();
    Set<String> regionSet = new HashSet();
    
    for (PublishJobVO vo : vos) {
      jobSet.add(vo.getPk_job());
      orgSet.add(vo.getPk_org());
      defdocSet.add(vo.getEducational());
      defdocSet.add(vo.getDegree());
      defdocSet.add(vo.getProfessional());
      defdocSet.add(vo.getTech());
      defdocSet.add(vo.getFroeignlang());
      
      regionSet.add(vo.getDomicile());
    }
    jobSet.remove(null);
    orgSet.remove(null);
    defdocSet.remove(null);
    regionSet.remove(null);
    
    Map<String, AggRMJobVO> jobMap = getRmJobQuery().queryByPk((String[])jobSet.toArray(new String[0]));
    OrgVO[] orgVOs = getOrgQuery().getOrgs((String[])orgSet.toArray(new String[0]));
    DefdocVO[] docvos = getDefdocQueryService().queryDefdocByPk((String[])defdocSet.toArray(new String[0]));
    RegionVO[] regionvos = null;
    if (!CollectionUtils.isEmpty(regionSet))
      regionvos = (RegionVO[])getServiceTemplate().queryByPks(RegionVO.class, (String[])regionSet.toArray(new String[0]));
    Map<String, OrgVO> orgMap = CommonUtils.toMap("pk_org", orgVOs);
    Map<String, DefdocVO> defdocMap = CommonUtils.toMap("pk_defdoc", docvos);
    Map<String, RegionVO> regionMap = CommonUtils.toMap("pk_region", regionvos);
    

    for (PublishJobVO vo : vos) {
      vo.getPropertyMap().put("jobname", MultiLangHelper.getName((SuperVO)((AggRMJobVO)jobMap.get(vo.getPk_job())).getParentVO()));
      vo.getPropertyMap().put("rmorgname", MultiLangHelper.getName((SuperVO)orgMap.get(vo.getPk_rmorg())));
      StringBuffer sb = new StringBuffer();
      sb.append(ResHelper.getString("6021publish", "06021publish0020") + MultiLangHelper.getName((SuperVO)defdocMap.get(vo.getEducational())));
      sb.append(ResHelper.getString("6021publish", "06021publish0021") + MultiLangHelper.getName((SuperVO)defdocMap.get(vo.getDegree())));
      sb.append(ResHelper.getString("6021publish", "06021publish0022") + MultiLangHelper.getName((SuperVO)defdocMap.get(vo.getProfessional())));
      sb.append(ResHelper.getString("6021publish", "06021publish0023") + vo.getWorkyear());
      sb.append(vo.getSex().equals(Integer.valueOf(1)) ? ResHelper.getString("6021publish", "06021publish0025") : vo.getSex() == null ? "" : ResHelper.getString("6021publish", "06021publish0026"));
      sb.append(ResHelper.getString("6021publish", "06021publish0027") + vo.getMinage().intValue());
      sb.append(ResHelper.getString("6021publish", "06021publish0028") + vo.getMaxage().intValue());
      sb.append(ResHelper.getString("6021publish", "06021publish0024") + MultiLangHelper.getName((SuperVO)defdocMap.get(vo.getTech())));
      sb.append(ResHelper.getString("6021publish", "06021publish0029") + MultiLangHelper.getName((SuperVO)regionMap.get(vo.getDomicile())));
      sb.append(ResHelper.getString("6021publish", "06021publish0030") + MultiLangHelper.getName((SuperVO)defdocMap.get(vo.getFroeignlang())));
      
      sb.append(ResHelper.getString("6021publish", "06021publish0031") + vo.getFrolanlevel());
      sb.append(ResHelper.getString("6021publish", "06021publish0032") + vo.getQualification());
      vo.getPropertyMap().put("qualif", sb.toString());
    }
    return vos;
  }
  
  public AggPublishVO queryByPk(String pk_publishjob) throws BusinessException
  {
    if (StringUtils.isEmpty(pk_publishjob))
      return null;
    AggPublishVO aggvo = (AggPublishVO)getServiceTemplate().queryByPk(AggPublishVO.class, pk_publishjob);
    PublishJobVO mainvo = (PublishJobVO)aggvo.getParentVO();
    String pk_job = mainvo.getPk_job();
    
    if (!StringUtils.isEmpty(pk_job)) {
      RMJobVO jobvo = getRmJobQuery().queryByPk(pk_job);
      if (jobvo != null) {
        mainvo.getPropertyMap().put("jobname", jobvo.getName());
      }
    }
    String pk_rmorg = mainvo.getPk_rmorg();
    if (StringUtils.isNotBlank(pk_rmorg)) {
      OrgVO orgvo = getOrgQuery().getOrg(pk_rmorg);
      if (orgvo != null) {
        mainvo.getPropertyMap().put("rmorgname", orgvo.getName());
      }
    }
    
    String qualif = "";
    String add = "";
    if (StringUtils.isNotBlank(mainvo.getEducational())) {
      DefdocVO[] docvos = getDefdocQueryService().queryDefdocByPk(new String[] { mainvo.getEducational() });
      if (!ArrayUtils.isEmpty(docvos)) {
        add = ResHelper.getString("6021publish", "06021publish0020") + docvos[0].getName();
        qualif = getQulifAdd(qualif, add);
      }
    }
    if (StringUtils.isNotBlank(mainvo.getDegree())) {
      DefdocVO[] docvos = getDefdocQueryService().queryDefdocByPk(new String[] { mainvo.getDegree() });
      if (!ArrayUtils.isEmpty(docvos)) {
        add = ResHelper.getString("6021publish", "06021publish0021") + docvos[0].getName();
        qualif = getQulifAdd(qualif, add);
      }
    }
    if (StringUtils.isNotBlank(mainvo.getProfessional())) {
      DefdocVO[] docvos = getDefdocQueryService().queryDefdocByPk(new String[] { mainvo.getProfessional() });
      if (!ArrayUtils.isEmpty(docvos)) {
        add = ResHelper.getString("6021publish", "06021publish0022") + docvos[0].getName();
        qualif = getQulifAdd(qualif, add);
      }
    }
    if (mainvo.getWorkyear() != null) {
      add = ResHelper.getString("6021publish", "06021publish0023") + mainvo.getWorkyear().intValue();
      qualif = getQulifAdd(qualif, add);
    }
    if (StringUtils.isNotBlank(mainvo.getTech())) {
      DefdocVO[] docvos = getDefdocQueryService().queryDefdocByPk(new String[] { mainvo.getTech() });
      if (!ArrayUtils.isEmpty(docvos)) {
        add = ResHelper.getString("6021publish", "06021publish0024") + docvos[0].getName();
        qualif = getQulifAdd(qualif, add);
      }
    }
    if (mainvo.getSex() != null) {
      if (mainvo.getSex().equals(Integer.valueOf(1))) {
        add = ResHelper.getString("6021publish", "06021publish0025");
        qualif = getQulifAdd(qualif, add);
      } else {
        add = ResHelper.getString("6021publish", "06021publish0026");
        qualif = getQulifAdd(qualif, add);
      }
    }
    if (mainvo.getMinage() != null) {
      add = ResHelper.getString("6021publish", "06021publish0027") + mainvo.getMinage().intValue();
      qualif = getQulifAdd(qualif, add);
    }
    if (mainvo.getMaxage() != null) {
      add = ResHelper.getString("6021publish", "06021publish0028") + mainvo.getMaxage().intValue();
      qualif = getQulifAdd(qualif, add);
    }
    if (StringUtils.isNotBlank(mainvo.getDomicile())) {
      DefdocVO[] docvos = getDefdocQueryService().queryDefdocByPk(new String[] { mainvo.getDomicile() });
      if (!ArrayUtils.isEmpty(docvos)) {
        add = ResHelper.getString("6021publish", "06021publish0029") + docvos[0].getName();
        qualif = getQulifAdd(qualif, add);
      }
    }
    if (StringUtils.isNotBlank(mainvo.getFroeignlang())) {
      DefdocVO[] docvos = getDefdocQueryService().queryDefdocByPk(new String[] { mainvo.getFroeignlang() });
      if (!ArrayUtils.isEmpty(docvos)) {
        add = ResHelper.getString("6021publish", "06021publish0030") + docvos[0].getName();
        qualif = getQulifAdd(qualif, add);
      }
    }
    if (StringUtils.isNotBlank(mainvo.getFrolanlevel()))
    {




      add = ResHelper.getString("6021publish", "06021publish0031") + mainvo.getFrolanlevel();
      qualif = getQulifAdd(qualif, add);
    }
    if (StringUtils.isNotBlank(mainvo.getQualification())) {
      add = ResHelper.getString("6021publish", "06021publish0032") + mainvo.getQualification();
      qualif = getQulifAdd(qualif, add);
    }
    
    mainvo.getPropertyMap().put("qualif", qualif);
    return aggvo;
  }
  
  private String getQulifAdd(String qualif, String add) {
    if (StringUtils.isBlank(add))
      return qualif;
    if (StringUtils.isBlank(qualif))
      return add;
    return qualif + ";" + add;
  }
  
  public Map<String, AggRMJobVO> queryByPk(String[] pk_publishjobs) throws BusinessException
  {
    if (ArrayUtils.isEmpty(pk_publishjobs))
      return null;
    String publishInSQL = StringPiecer.getDefaultPiecesTogether(pk_publishjobs);
    String publishCond = "pk_publishjob in (" + publishInSQL + ") ";
    PublishJobVO[] publishvos = (PublishJobVO[])CommonUtils.retrieveByClause(PublishJobVO.class, publishCond);
    String[] pk_jobs = StringPiecer.getStrArray(publishvos, "pk_job");
    AggRMJobVO[] aggJobvos = getRmJobQuery().queryByPks(pk_jobs);
    if (ArrayUtils.isEmpty(aggJobvos))
      return null;
    Map<String, AggRMJobVO> jobMap = new HashMap();
    for (PublishJobVO publishvo : publishvos) {
      for (AggRMJobVO aggjobvo : aggJobvos) {
        RMJobVO jobvo = (RMJobVO)aggjobvo.getParentVO();
        if (publishvo.getPk_job().equals(jobvo.getPk_job()))
          jobMap.put(publishvo.getPk_publishjob(), aggjobvo);
      }
    }
    return jobMap;
  }
  
  public AggRMJobVO queryJobByPk(String pk_publishjob) throws BusinessException
  {
    AggPublishVO aggvo = (AggPublishVO)getServiceTemplate().queryByPk(AggPublishVO.class, pk_publishjob);
    if (aggvo == null)
      return null;
    String pk_job = ((PublishJobVO)aggvo.getParentVO()).getPk_job();
    return getRmJobQuery().queryAggJobVOByPk(pk_job);
  }
  
  public AggPublishVO[] queryByPlace(FromWhereSQL fromWhereSQL, Integer type) throws BusinessException
  {
    if ((type == null) && (fromWhereSQL == null))
      return null;
    String alias = FromWhereSQLUtils.getMainTableAlias(fromWhereSQL, PublishJobVO.getDefaultTableName());
    
    UFLiteralDate curDate = PubEnv.getServerLiteralDate();
    String condition = alias + "." + "publishstatus" + " = 1 and " + alias + "." + "publishdate" + " <= '" + curDate + "' and (" + alias + "." + "enddate" + " >= '" + curDate + "' or isnull(" + alias + "." + "enddate" + ", '~')='~')";
    


    if ((fromWhereSQL != null) && (fromWhereSQL.getWhere() != null)) {
      String inSql = FromWhereSQLUtils.createSelectSQL(fromWhereSQL, PublishJobVO.getDefaultTableName(), new String[] { "pk_publishjob" }, null, null, null, null);
      condition = condition + " and " + alias + "." + "pk_publishjob" + " in ( " + inSql + " ) ";
    }
    if (type != null) {
      condition = condition + " and " + alias + "." + "pk_publishjob" + " in ( select place.pk_publishjob from rm_publishplace place where place.place = " + type.intValue() + " ) ";
    }
    return (AggPublishVO[])getServiceTemplate().queryByCondition(AggPublishVO.class, condition);
  }
  
  public AggPublishVO[] queryPublishJobByActive(String pk_active, String pk_jobType, String workplace, UFLiteralDate publishDate, String jobName,Integer type)
    throws BusinessException
  {
    if (StringUtils.isEmpty(pk_active))
      return null;
    AggActiveVO aggActiveVO = getActiveQuery().queryByPk(pk_active);
    if (aggActiveVO == null)
      return null;
    ActiveJobVO[] jobvos = (ActiveJobVO[])aggActiveVO.getTableVO("sub_jobinfo");
    if (ArrayUtils.isEmpty(jobvos))
      return null;
    String inSql = StringPiecer.getDefaultPiecesTogether(jobvos, "pk_jobinfo");
    
    UFLiteralDate curDate = PubEnv.getServerLiteralDate();
    String condition = "pk_jobinfo in (" + inSql + ") and publishstatus = 1 and " + "publishdate" + " <= '" + curDate + "' and (" + "enddate" + " >= '" + curDate + "' or isnull(" + "enddate" + ",'~')='~')";
    //String condition = "pk_jobinfo in (" + inSql + ") and publishstatus = 1";


    if (StringUtils.isNotBlank(pk_jobType)) {
      condition = condition + " and pk_job in (select pk_job from rm_job where pk_jobtype = '" + pk_jobType + "' ) ";
    }
    if (StringUtils.isNotBlank(workplace))
      condition = condition + " and workplace = '" + workplace + "' ";
    if (publishDate != null)
      condition = condition + " and publishdate >= '" + publishDate + "'  ";
    if (StringUtils.isNotBlank(jobName)) {
      condition = condition + " and pk_job in (select pk_job from rm_job where name like '%" + jobName + "%') ";
    }
    condition = condition + " and pk_publishjob in ( select place.pk_publishjob from rm_publishplace place where place.place = "+type+") ";
    return (AggPublishVO[])getServiceTemplate().queryByCondition(AggPublishVO.class, condition);
  }
  
  public PublishJobVO[] queryByWeb(String pk_org, String pk_web, String[] notIncPks) throws BusinessException
  {
    if (StringUtils.isEmpty(pk_web))
      return null;
    InSQLCreator inSqlCreator = null;
    try {
      String condition = " publishstatus = 1 and pk_publishjob in (select pk_publishjob from rm_publishplace where pk_web = '" + pk_web + "') ";
      if (StringUtils.isNotBlank(pk_org))
        condition = condition + " and pk_org = '" + pk_org + "'";
      if (!ArrayUtils.isEmpty(notIncPks)) {
        inSqlCreator = new InSQLCreator();
        condition = condition + " and pk_publishjob not in ( " + inSqlCreator.getInSQL(notIncPks) + ")";
      }
      return ensureDetailInfos((PublishJobVO[])CommonUtils.retrieveByClause(PublishJobVO.class, condition));
    } finally {
      if (inSqlCreator != null) {
        inSqlCreator.clear();
      }
    }
  }
  
  public PublishJobVO[] queryByEndDate(String pk_org, UFLiteralDate endDate) throws BusinessException {
    if ((pk_org == null) || (endDate == null))
      return null;
    String condition = "pk_org = '" + pk_org + "' and " + "publishstatus" + " = " + PublishStatusEnum.PUBLISHED.toIntValue() + " and " + "enddate" + " = '" + endDate + "' ";
    

    return (PublishJobVO[])CommonUtils.retrieveByClause(PublishJobVO.class, condition);
  }
  
  public AggPublishVO[] queryByCondition(String condition) throws BusinessException
  {
    if (StringUtils.isEmpty(condition))
      return null;
    return (AggPublishVO[])getServiceTemplate().queryByCondition(AggPublishVO.class, condition);
  }
}
