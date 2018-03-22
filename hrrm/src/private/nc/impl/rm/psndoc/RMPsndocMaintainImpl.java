package nc.impl.rm.psndoc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nc.bs.bd.baseservice.busilog.BDBusiLogUtil;
import nc.bs.bd.cache.CacheProxy;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.core.util.ObjectCreator;
import nc.bs.logging.Logger;
import nc.bs.uif2.validation.DefaultValidationService;
import nc.hr.frame.persistence.SimpleDocServiceTemplate;
import nc.hr.utils.FromWhereSQLUtils;
import nc.hr.utils.InSQLCreator;
import nc.hr.utils.MultiLangHelper;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.hr.utils.SQLHelper;
import nc.hr.utils.StringPiecer;
import nc.impl.rm.psndoc.validator.RMPsndocDeleteValidator;
import nc.impl.rm.psndoc.validator.RMPsndocSameJobValidator;
import nc.impl.rm.webservice.AbstractWebInfoExecutor;
import nc.itf.hi.IBlacklistManageService;
import nc.itf.hi.IPsndocQryService;
import nc.itf.rm.IActiveManageService;
import nc.itf.rm.ICheckinQueryService;
import nc.itf.rm.IHireQueryService;
import nc.itf.rm.IInterviewQueryService;
import nc.itf.rm.IPublishQueryService;
import nc.itf.rm.IRMPsndocManageMaintain;
import nc.itf.rm.IRMPsndocManageService;
import nc.itf.rm.IRMPsndocQueryMaintain;
import nc.itf.rm.IRMPsndocQueryService;
import nc.itf.rm.IWebInfoQueryService;
import nc.itf.trn.transmng.ITransmngManageService;
import nc.md.data.access.NCObject;
import nc.md.persist.framework.IMDPersistenceQueryService;
import nc.pubitf.para.SysInitQuery;
import nc.ui.querytemplate.querytree.FromWhereSQL;
import nc.vo.hi.blacklist.BlacklistVO;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hi.psndoc.PsndocAggVO;
import nc.vo.hi.psndoc.PsndocVO;
import nc.vo.hi.psndoc.enumeration.TrnseventEnum;
import nc.vo.hi.pub.RM2TRNLinkData;
import nc.vo.hr.frame.persistence.BooleanProcessor;
import nc.vo.hr.tools.pub.GeneralVO;
import nc.vo.hr.tools.pub.GeneralVOProcessor;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.rm.checkin.AggCheckinVO;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.job.AggRMJobVO;
import nc.vo.rm.job.RMCPModelVO;
import nc.vo.rm.job.RMJobVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.PrimaryResultVO;
import nc.vo.rm.psndoc.RMEduVO;
import nc.vo.rm.psndoc.RMPsnCPVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.RMPsndocVO;
import nc.vo.rm.psndoc.common.RMApplyStatusEnum;
import nc.vo.rm.psndoc.common.RMApplyTypeEnum;
import nc.vo.rm.psndoc.common.ResumeSourceEnum;
import nc.vo.rm.psndoc.validator.RMPsndocDateValidator;
import nc.vo.rm.publish.AggPublishVO;
import nc.vo.rm.publish.PublishJobVO;
import nc.vo.rm.webinfo.WebInfoVO;
import nc.vo.uif2.LoginContext;
import nc.vo.util.BDVersionValidationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class RMPsndocMaintainImpl
  implements IRMPsndocManageMaintain, IRMPsndocQueryMaintain
{
  private SimpleDocServiceTemplate serviceTemplate;

  public SimpleDocServiceTemplate getServiceTemplate()
  {
    if (this.serviceTemplate == null)
      this.serviceTemplate = new SimpleDocServiceTemplate("05654a1a-6469-4744-963a-fa0a06e0b01f");
    return this.serviceTemplate;
  }

  public void delete(AggRMPsndocVO vo) throws BusinessException
  {
    if (vo == null) {
      return;
    }
    DefaultValidationService vService = new DefaultValidationService();
    vService.addValidator(new RMPsndocDeleteValidator());
    vService.validate(vo);

    AggRMPsndocVO[] oldVOs = processResult(vo.getPsndocVO().getPk_org(), -1, false, new AggRMPsndocVO[] { queryByPK(vo.getPsndocVO().getPk_psndoc()) });
    String[] pk_actives = getActiveArray(ArrayUtils.isEmpty(oldVOs) ? null : (RMPsnJobVO[])oldVOs[0].getTableVO(RMPsnJobVO.getDefaultTableName()));
    if (!ArrayUtils.isEmpty(pk_actives))
      ((IActiveManageService)NCLocator.getInstance().lookup(IActiveManageService.class)).updateFactRegNum(Integer.valueOf(-1), pk_actives);
    BaseDAO dao = new BaseDAO();

    String sql = "select top 1 1 from rm_psndoc_job where pk_psndoc = '" + vo.getPsndocVO().getPk_psndoc() + "' and (pk_org <> '" + vo.getPsndocVO().getPk_org() + 
      "' or (pk_org='" + vo.getPsndocVO().getPk_org() + "' and applystatus = " + RMApplyStatusEnum.INDOC.toIntValue() + ")) ";
    Boolean hasJob = (Boolean)new BaseDAO().executeQuery(sql, new BooleanProcessor());

    if (hasJob.booleanValue()) {
      String delCPSql = "delete from rm_psndoc_cp where pk_psndoc = '" + vo.getPsndocVO().getPk_psndoc() + 
        "' and pk_job in (select pk_reg_job from rm_psndoc_job where pk_psndoc = '" + vo.getPsndocVO().getPk_psndoc() + 
        "' and pk_org = '" + vo.getPsndocVO().getPk_org() + "' and applystatus <> " + RMApplyStatusEnum.INDOC.toIntValue() + ") ";
      String delJobSql = "delete from rm_psndoc_job where pk_psndoc = '" + vo.getPsndocVO().getPk_psndoc() + 
        "' and pk_org = '" + vo.getPsndocVO().getPk_org() + "' and applystatus <> " + RMApplyStatusEnum.INDOC.toIntValue();
      dao.executeUpdate(delCPSql);
      dao.executeUpdate(delJobSql);
      return;
    }

    getServiceTemplate().delete(vo);
    CacheProxy.fireDataDeleted(RMPsndocVO.getDefaultTableName(), vo.getPsndocVO().getPk_psndoc());
  }

  public void deleteReEmployee(AggRMPsndocVO vo) throws BusinessException
  {
    delete(vo);
  }

  public void downloadResume(String pk_org, String[] webPKs) throws BusinessException
  {
    if (ArrayUtils.isEmpty(webPKs))
      return;
    Map webInfoMap = ((IWebInfoQueryService)NCLocator.getInstance().lookup(IWebInfoQueryService.class)).queryWebInfoMap();
    for (String webPK : webPKs) {
      WebInfoVO webInfo = (WebInfoVO)webInfoMap.get(webPK);
      AbstractWebInfoExecutor impl = (AbstractWebInfoExecutor)ObjectCreator.newInstance("hrrm", webInfo.getImplclass());
      impl.setWebInfo(webInfo);
      impl.downloadResume(pk_org);
    }
  }

  public Object insertWithCheck(AggRMPsndocVO vo)
    throws BusinessException
  {
    String checkResult = checkUniqueInfo(vo.getPsndocVO());
    if (checkResult != null)
      return checkResult;
    return insertWithoutCheck(vo);
  }

  public String checkUniqueInfo(RMPsndocVO psndocVO)
    throws BusinessException
  {
    BlacklistVO blacklistVO = new BlacklistVO();
    blacklistVO.setIdtype(psndocVO.getIdtype());
    blacklistVO.setId(psndocVO.getId());
    blacklistVO.setPsnname(psndocVO.getName());
    blacklistVO.setPsnname2(psndocVO.getName2());
    blacklistVO.setPsnname3(psndocVO.getName3());
    blacklistVO.setPsnname4(psndocVO.getName4());
    blacklistVO.setPsnname5(psndocVO.getName5());
    blacklistVO.setPsnname6(psndocVO.getName6());
    if (((IBlacklistManageService)NCLocator.getInstance().lookup(IBlacklistManageService.class)).isInBlacklist(blacklistVO)) {
      throw new BusinessException(ResHelper.getString("6021psndoc", "06021psndoc0076"));
    }
    IPsndocQryService psndocQryService = (IPsndocQryService)NCLocator.getInstance().lookup(IPsndocQryService.class);

    PsndocAggVO hiPsnAggVO = psndocQryService.queryPsndocByNameID(psndocVO.getName(), psndocVO.getIdtype(), psndocVO.getId());

    AggRMPsndocVO aggVO = new RMPsndocServiceImpl().queryPsndocByUniqueRule(psndocVO);

    boolean isReApplyFromRM = false;

    if (hiPsnAggVO != null)
    {
      if (hiPsnAggVO.getParentVO().getDie_date() != null) {
        throw new BusinessException(ResHelper.getString("6021psndoc", "06021psndoc0099"));
      }
      if (psndocQryService.isInJob(hiPsnAggVO.getParentVO().getPk_psndoc())) {
        throw new BusinessException(ResHelper.getString("6021psndoc", "06021psndoc0034"));
      }
      if (aggVO != null) {
        isReApplyFromRM = true;
      }
    }

    if (aggVO == null) {
      return null;
    }
    boolean isPsnlib = false;

    boolean hasApply = false;
    RMPsnJobVO[] jobVOs = (RMPsnJobVO[])aggVO.getTableVO(RMPsnJobVO.getDefaultTableName());
    int i = 0; for (int j = ArrayUtils.getLength(jobVOs); i < j; i++) {
      if (!jobVOs[i].isUse())
        continue;
      isPsnlib = (isPsnlib) || (jobVOs[i].isPsnlib());
      hasApply = (hasApply) || (RMApplyStatusEnum.INDOC.toIntValue() != jobVOs[i].getApplystatus().intValue());

      if ((isPsnlib) && (jobVOs[i].getPk_org().equals(psndocVO.getPk_org())))
        throw new BusinessException(ResHelper.getString("6021psndoc", "06021psndoc0035"));
      if (isPsnlib)
        return ResHelper.getString("6021psndoc", "06021psndoc0108");
    }
    if (!isPsnlib) {
      isPsnlib = aggVO.getPsndocVO().getIsshare().booleanValue();
    }
    if (hasApply) {
      return ResHelper.getString("6021psndoc", "06021psndoc0037");
    }
    if (isReApplyFromRM) {
      return "reApplyFromRM";
    }

    return null;
  }

  public AggRMPsndocVO insertWithoutCheck(AggRMPsndocVO vo)
    throws BusinessException
  {
    DefaultValidationService vService = new DefaultValidationService();
    vService.addValidator(new RMPsndocDateValidator());
    vService.addValidator(new RMPsndocSameJobValidator());
    vService.validate(vo);

    RMPsndocVO headVO = vo.getPsndocVO();
    headVO.setUpdatetime(PubEnv.getServerTime());
    IPsndocQryService psndocQryService = (IPsndocQryService)NCLocator.getInstance().lookup(IPsndocQryService.class);

    PsndocAggVO hiPsnAggVO = psndocQryService.queryPsndocByNameID(headVO.getName(), headVO.getIdtype(), headVO.getId());

    if (hiPsnAggVO != null) {
      headVO.setApplytype(Integer.valueOf(RMApplyTypeEnum.REAPPLY.toIntValue()));
      headVO.setApplystatus(Integer.valueOf(RMApplyStatusEnum.INIT.toIntValue()));
      headVO.setPk_hipsndoc(hiPsnAggVO.getParentVO().getPk_psndoc());
    }
    return directInsert(vo);
  }

  public AggRMPsndocVO overridePsndoc(AggRMPsndocVO vo)
    throws BusinessException
  {
    AggRMPsndocVO aggVO = new RMPsndocServiceImpl().queryPsndocByUniqueRule(vo.getPsndocVO());
    if (aggVO == null)
      return null;
    RMPsndocVO headVO = vo.getPsndocVO();
    RMPsndocVO psndocVO = aggVO.getPsndocVO();
    headVO.setPk_psndoc(psndocVO.getPk_psndoc());
    headVO.setCreator(psndocVO.getCreator());
    headVO.setCreationtime(psndocVO.getCreationtime());
    headVO.setStatus(1);

    headVO.setTs(psndocVO.getTs());

    headVO.setUpdatetime(PubEnv.getServerTime());

    IPsndocQryService psndocQryService = (IPsndocQryService)NCLocator.getInstance().lookup(IPsndocQryService.class);
    PsndocAggVO hiPsnAggVO = psndocQryService.queryPsndocByNameID(headVO.getName(), headVO.getIdtype(), headVO.getId());

    if (hiPsnAggVO != null) {
      headVO.setApplytype(Integer.valueOf(RMApplyTypeEnum.REAPPLY.toIntValue()));
      headVO.setPk_hipsndoc(hiPsnAggVO.getParentVO().getPk_psndoc());
    }

    for (String tableCode : vo.getTableCodes())
    {
      if ((RMPsnJobVO.getDefaultTableName().equals(tableCode)) || (RMPsnCPVO.getDefaultTableName().equals(tableCode)))
        continue;
      List childList = new ArrayList();
      if (!ArrayUtils.isEmpty(vo.getTableVO(tableCode))) {
        CollectionUtils.addAll(childList, vo.getTableVO(tableCode));
      }
      for (SuperVO childVO : childList)
      {
        childVO.setAttributeValue("ts", new UFDateTime());
      }

      SuperVO[] childVOs = (SuperVO[])aggVO.getTableVO(tableCode);
      if (!ArrayUtils.isEmpty(childVOs)) {
        for (SuperVO childVO : childVOs) {
          childVO.setStatus(3);
          childList.add(childVO);
        }
      }
      vo.setTableVO(tableCode, CollectionUtils.isEmpty(childList) ? null : (SuperVO[])childList.toArray(new SuperVO[0]));
    }
    return directUpdate(vo);
  }

  private String[] getActiveArray(RMPsnJobVO[] vos)
  {
    if (ArrayUtils.isEmpty(vos))
      return null;
    Set retSet = new HashSet();
    for (RMPsnJobVO vo : vos)
    {
      if (3 == vo.getStatus())
        continue;
      String str = vo.getPk_active();

      if ((StringUtils.isEmpty(str)) || (retSet.contains(str)))
        continue;
      retSet.add(str);
    }
    return (String[])retSet.toArray(new String[0]);
  }

  private String[] minusStringArrays(String[] array1, String[] array2)
  {
    if (ArrayUtils.isEmpty(array1))
      return null;
    if (ArrayUtils.isEmpty(array2))
      return array1;
    List resultList = new ArrayList();
    for (String array : array1) {
      if (!ArrayUtils.contains(array2, array))
        continue;
      resultList.add(array);
    }
    return CollectionUtils.isEmpty(resultList) ? null : (String[])resultList.toArray(new String[0]);
  }

  public AggRMPsndocVO directInsert(AggRMPsndocVO vo)
    throws BusinessException
  {
    int applyStatus = vo.getPsndocVO().getApplystatus().intValue();

    if (ArrayUtils.isEmpty(vo.getTableVO(RMPsnJobVO.getDefaultTableName()))) {
      RMPsnJobVO jobVO = createAbstractJob(vo.getPsndocVO());
      vo.setTableVO(RMPsnJobVO.getDefaultTableName(), new RMPsnJobVO[] { jobVO });
    }

    String[] pk_actives = getActiveArray((RMPsnJobVO[])vo.getTableVO(RMPsnJobVO.getDefaultTableName()));
    if (!ArrayUtils.isEmpty(pk_actives)) {
      ((IActiveManageService)NCLocator.getInstance().lookup(IActiveManageService.class)).updateFactRegNum(Integer.valueOf(1), pk_actives);
    }
    vo = (AggRMPsndocVO)getServiceTemplate().insert(vo);
    CacheProxy.fireDataInserted(RMPsndocVO.getDefaultTableName());
    AggRMPsndocVO[] result = new RMPsndocMaintainImpl().processResult(vo.getPsndocVO().getPk_org(), applyStatus, false, new AggRMPsndocVO[] { vo });
    return ArrayUtils.isEmpty(result) ? null : result[0];
  }

  public AggRMPsndocVO directUpdate(AggRMPsndocVO vo)
    throws BusinessException
  {
    BDVersionValidationUtil.validateSuperVO(
      new SuperVO[] { vo.getPsndocVO() });
    int applyStatus = vo.getPsndocVO().getApplystatus().intValue();

    RMPsnJobVO absJob = createAbstractJob(vo.getPsndocVO());

    AggRMPsndocVO[] oldVOs = processResult(vo.getPsndocVO().getPk_org(), -1, false, new AggRMPsndocVO[] { queryByPK(vo.getPsndocVO().getPk_psndoc()) });
    vo = (AggRMPsndocVO)getServiceTemplate().update(vo, true);

    String pk_org = vo.getPsndocVO().getPk_org();
    RMPsnJobVO[] jobVOs = (RMPsnJobVO[])vo.getTableVO(RMPsnJobVO.getDefaultTableName());

    boolean hasAbstract = false;
    int jobCount = 0;
    int i = 0; for (int j = ArrayUtils.getLength(jobVOs); i < j; i++) {
      if (!StringUtils.equals(pk_org, jobVOs[i].getPk_org()))
        continue;
      if ((RMApplyStatusEnum.INDOC.toIntValue() != jobVOs[i].getApplystatus().intValue()) && (RMApplyStatusEnum.INIT.toIntValue() != jobVOs[i].getApplystatus().intValue()))
        jobCount++;
      if ("0000Z700000000000000".equals(jobVOs[i].getPk_reg_job()))
        hasAbstract = true;
    }
    BaseDAO baseDAO = new BaseDAO();

    if (jobCount == 0) {
      vo.getPsndocVO().setApplystatus(Integer.valueOf(applyStatus));
      baseDAO.insertVO(absJob);
    }
    else if ((hasAbstract) && (jobCount > 1)) {
      String delSql = "delete from rm_psndoc_job where pk_psndoc='" + vo.getPsndocVO().getPk_psndoc() + "' and pk_reg_job='" + "0000Z700000000000000" + "' and pk_org='" + pk_org + "'  and APPLYSTATUS in(0,1) ";
      baseDAO.executeUpdate(delSql);
    }

    AggRMPsndocVO[] newVOs = processResult(vo.getPsndocVO().getPk_org(), -1, false, new AggRMPsndocVO[] { vo });
    String[] oldActives = getActiveArray(ArrayUtils.isEmpty(oldVOs) ? null : (RMPsnJobVO[])oldVOs[0].getTableVO(RMPsnJobVO.getDefaultTableName()));
    String[] newActives = getActiveArray(ArrayUtils.isEmpty(newVOs) ? null : (RMPsnJobVO[])newVOs[0].getTableVO(RMPsnJobVO.getDefaultTableName()));
    String[] delActives = minusStringArrays(oldActives, newActives);
    String[] addActives = minusStringArrays(newActives, oldActives);
    IActiveManageService actService = (IActiveManageService)NCLocator.getInstance().lookup(IActiveManageService.class);
    if (!ArrayUtils.isEmpty(delActives))
      actService.updateFactRegNum(Integer.valueOf(-1), delActives);
    if (!ArrayUtils.isEmpty(addActives)) {
      actService.updateFactRegNum(Integer.valueOf(1), addActives);
    }

    vo = queryByPK(vo.getPsndocVO().getPk_psndoc());
    CacheProxy.fireDataUpdated(RMPsndocVO.getDefaultTableName());
    AggRMPsndocVO[] result = new RMPsndocMaintainImpl().processResult(pk_org, applyStatus, false, new AggRMPsndocVO[] { vo });
    return ArrayUtils.isEmpty(result) ? null : result[0];
  }

  private RMPsnJobVO createAbstractJob(RMPsndocVO vo)
  {
    RMPsnJobVO jobVO = new RMPsnJobVO();
    jobVO.setPk_psndoc(vo.getPk_psndoc());
    jobVO.setPk_reg_job("0000Z700000000000000");
    jobVO.setPk_group(vo.getPk_group());
    jobVO.setPk_org(vo.getPk_org());
    jobVO.setApplystatus(vo.getApplystatus());
    jobVO.setApplystatusdate(PubEnv.getServerLiteralDate());
    jobVO.setStatus(2);
    jobVO.setIsuse(UFBoolean.TRUE);
    jobVO.setIspsnlib(UFBoolean.FALSE);
    jobVO.setReg_date(PubEnv.getServerLiteralDate());
    jobVO.setSourcetype(Integer.valueOf(ResumeSourceEnum.MANUAL.toIntValue()));
    return jobVO;
  }

  public AggRMPsndocVO insertInApply(AggRMPsndocVO vo)
    throws BusinessException
  {
    DefaultValidationService vService = new DefaultValidationService();
    vService.addValidator(new RMPsndocDateValidator());
    vService.addValidator(new RMPsndocSameJobValidator());
    vService.validate(vo);

    RMPsndocVO headVO = vo.getPsndocVO();
    headVO.setUpdatetime(PubEnv.getServerTime());
    return directInsert(vo);
  }

  public AggRMPsndocVO update(AggRMPsndocVO vo)
    throws BusinessException
  {
    DefaultValidationService vService = new DefaultValidationService();
    vService.addValidator(new RMPsndocDateValidator());
    vService.addValidator(new RMPsndocSameJobValidator());
    vService.validate(vo);

    setEduInfoForPsndoc(vo);
    return directUpdate(vo);
  }

  public AggRMPsndocVO[] intoApplyFromREApply(AggRMPsndocVO[] vos)
    throws BusinessException
  {
    if (ArrayUtils.isEmpty(vos))
      return null;
    String pk_org = vos[0].getPsndocVO().getPk_org();
    List pkList = new ArrayList();
    for (int i = 0; i < vos.length; i++)
      pkList.add(vos[i].getPsndocVO().getPk_psndoc());
    String[] pk_psndocs = (String[])pkList.toArray(new String[0]);
    InSQLCreator isc = new InSQLCreator();
    try {
      String cond = "pk_psndoc in (" + isc.getInSQL(pk_psndocs) + ") and pk_org = '" + pk_org + "' and applystatus = " + RMApplyStatusEnum.INIT.toIntValue();
      new RMPsndocDAO().updateApplyStatusByCond(RMApplyStatusEnum.APPLY.toIntValue(), cond);
    } finally {
      isc.clear();
    }
    vos = (AggRMPsndocVO[])getServiceTemplate().queryByPks(AggRMPsndocVO.class, pk_psndocs);
    return processResult(pk_org, RMApplyStatusEnum.APPLY.toIntValue(), false, vos);
  }

  protected AggRMPsndocVO[] processResult(String pk_org, int applyStatus, boolean showAll, AggRMPsndocVO[] vos)
    throws BusinessException
  {
    if (ArrayUtils.isEmpty(vos))
      return null;
    Map webInfoMap = ((IWebInfoQueryService)NCLocator.getInstance().lookup(IWebInfoQueryService.class)).queryWebInfoMap();
    List resultList = new ArrayList();

    for (AggRMPsndocVO aggvo : vos) {
      RMPsndocVO mainVO = aggvo.getPsndocVO();

      mainVO.setApplystatus(Integer.valueOf(applyStatus));

      RMPsnJobVO[] jobVOs = (RMPsnJobVO[])aggvo.getTableVO(RMPsnJobVO.getDefaultTableName());
      UFLiteralDate applyStatusDate = UFLiteralDate.getDate("1900-01-01");
      ResumeSourceEnum sourceType;
      if (!ArrayUtils.isEmpty(jobVOs))
      {
        boolean hasJob = false;
        List jobList = new ArrayList();
        for (RMPsnJobVO jobVO : jobVOs)
        {
          if ((!jobVO.isUse()) || ((StringUtils.isNotEmpty(pk_org)) && (!jobVO.getPk_org().equals(pk_org))) || 
            ((applyStatus != -1) && (applyStatus != jobVO.getApplystatus().intValue())) || (
            (!showAll) && (jobVO.isPsnlib())))
          {
            continue;
          }
          mainVO.setPk_group(jobVO.getPk_group());
          mainVO.setPk_org(jobVO.getPk_org());
          if (applyStatusDate.before(jobVO.getApplystatusdate()))
            applyStatusDate = jobVO.getApplystatusdate();
          hasJob = true;
          if ("0000Z700000000000000".equals(jobVO.getPk_reg_job())) {
            continue;
          }
          sourceType = (ResumeSourceEnum)ResumeSourceEnum.valueOf(ResumeSourceEnum.class, jobVO.getSourcetype());

          if (jobVO.getSourcetype().intValue() != ResumeSourceEnum.WEB.toIntValue())
            jobVO.setSource(sourceType.getName());
          else
            jobVO.setSource(((WebInfoVO)webInfoMap.get(jobVO.getPk_webinfo())).getMultilangName());
          jobList.add(jobVO);
        }

        if (!hasJob)
          continue;
        mainVO.setApplystatusdate(applyStatusDate);
        jobVOs = CollectionUtils.isEmpty(jobList) ? null : (RMPsnJobVO[])jobList.toArray(new RMPsnJobVO[0]);
        aggvo.setTableVO(RMPsnJobVO.getDefaultTableName(), jobVOs);
      }

      resultList.add(aggvo);

      String[] pk_jobs = StringPiecer.getStrArray(jobVOs, "pk_reg_job");
      RMPsnCPVO[] cpVOs = (RMPsnCPVO[])aggvo.getTableVO(RMPsnCPVO.getDefaultTableName());
      if ((StringUtils.isNotEmpty(pk_org)) && (!ArrayUtils.isEmpty(cpVOs))) {
        List cpList = new ArrayList();
        for (RMPsnCPVO cpVO : cpVOs) {
          if (!ArrayUtils.contains(pk_jobs, cpVO.getPk_job()))
            continue;
          cpList.add(cpVO);
        }
        aggvo.setTableVO(RMPsnCPVO.getDefaultTableName(), CollectionUtils.isEmpty(cpList) ? null : (RMPsnCPVO[])cpList.toArray(new RMPsnCPVO[0]));
      }
    }
    return CollectionUtils.isEmpty(resultList) ? null : (AggRMPsndocVO[])resultList.toArray(new AggRMPsndocVO[0]);
  }

  public PrimaryResultVO doPrimary(RMPsnJobVO[] jobVOs, boolean isPrimary) throws BusinessException
  {
    if (ArrayUtils.isEmpty(jobVOs)) {
      return null;
    }
    BDVersionValidationUtil.validateSuperVO(jobVOs);
    int oldStatus = isPrimary ? RMApplyStatusEnum.APPLY.toIntValue() : RMApplyStatusEnum.PRIMARY.toIntValue();
    int status = isPrimary ? RMApplyStatusEnum.PRIMARY.toIntValue() : RMApplyStatusEnum.APPLY.toIntValue();
    String pk_org = jobVOs[0].getPk_org();
    String[] pk_psnjobs = StringPiecer.getStrArray(jobVOs, "pk_psndoc_job");
    InSQLCreator isc = new InSQLCreator();
    try {
      String cond = "pk_psndoc_job in (" + isc.getInSQL(pk_psnjobs) + ") and pk_org = '" + pk_org + "' and applystatus = " + oldStatus;
      new RMPsndocDAO().updateApplyStatusByCond(status, cond);
    } finally {
      isc.clear();
    }
    String[] pk_psndocs = StringPiecer.getStrArrayDistinct(jobVOs, "pk_psndoc");
    AggRMPsndocVO[] chgVOs = (AggRMPsndocVO[])getServiceTemplate().queryByPks(AggRMPsndocVO.class, pk_psndocs);
    PrimaryResultVO resultVO = new PrimaryResultVO();
    resultVO.setFromVOs(processResult(pk_org, oldStatus, false, chgVOs));

    chgVOs = (AggRMPsndocVO[])getServiceTemplate().queryByPks(AggRMPsndocVO.class, pk_psndocs);
    resultVO.setToVOs(processResult(pk_org, status, false, chgVOs));
    if (!isPrimary)
      return resultVO;
    writeBusiLog(resultVO);
    return resultVO;
  }

  private void writeBusiLog(PrimaryResultVO resultVO) {
    if ((resultVO == null) || (ArrayUtils.isEmpty(resultVO.getToVOs())))
      return;
    AggRMPsndocVO[] aggvos = resultVO.getToVOs();
    RMPsnJobVO[] jobVOs = (RMPsnJobVO[])null;
    for (int i = 0; i < aggvos.length; i++) {
      RMPsnJobVO[] subJobVOs = (RMPsnJobVO[])aggvos[i].getTableVO(RMPsnJobVO.getDefaultTableName());
      jobVOs = (RMPsnJobVO[])ArrayUtils.addAll(jobVOs, subJobVOs);
    }
    try {
      new BDBusiLogUtil("85238c2d-9d69-48c8-99b0-6ee9de82606d").writeBusiLog("Pass", "", jobVOs);
    } catch (BusinessException e) {
      Logger.error(e.getMessage());
    }
  }

  public Map<Integer, Object[]> queryApplyPsns(LoginContext context, Map<Integer, FromWhereSQL> map) throws BusinessException
  {
    Map returnMap = new HashMap();

    Integer index = (Integer)RMApplyStatusEnum.APPLY.value();
    if (map.get(index) != null) {
      returnMap.put(index, queryApplyPsndocs(context.getPk_org(), (FromWhereSQL)map.get(index), index.intValue()));
    }
    index = (Integer)RMApplyStatusEnum.PRIMARY.value();
    if (map.get(index) != null) {
      returnMap.put(index, queryApplyPsndocs(context.getPk_org(), (FromWhereSQL)map.get(index), index.intValue()));
    }
    index = (Integer)RMApplyStatusEnum.INTERVIEW.value();
    if (map.get(index) != null) {
      AggInterviewVO[] vos = ((IInterviewQueryService)NCLocator.getInstance().lookup(IInterviewQueryService.class)).queryInterviewByFromWhereSQL(context, (FromWhereSQL)map.get(index), true);
      returnMap.put(index, vos);
    }

    index = (Integer)RMApplyStatusEnum.HIRE.value();
    if (map.get(index) != null) {
      AggRMPsndocVO[] vos = ((IHireQueryService)NCLocator.getInstance().lookup(IHireQueryService.class)).queryPsnHireInfoByFromWhereSQL(context, (FromWhereSQL)map.get(index));
      returnMap.put(index, vos);
    }

    index = (Integer)RMApplyStatusEnum.CHECKIN.value();
    if (map.get(index) != null) {
      AggCheckinVO[] vos = ((ICheckinQueryService)NCLocator.getInstance().lookup(ICheckinQueryService.class)).queryCheckinByFromWhereSQL(context, (FromWhereSQL)map.get(index));
      returnMap.put(index, vos);
    }
    return returnMap;
  }

  public Map<Integer, Object[]> queryApplyPsnFiltPhotos(LoginContext context, Map<Integer, FromWhereSQL> map) throws BusinessException {
    Map returnMap = new HashMap();

    Integer index = (Integer)RMApplyStatusEnum.APPLY.value();
    if (map.get(index) != null) {
      returnMap.put(index, queryApplyPsndocFilterPhs(context.getPk_org(), (FromWhereSQL)map.get(index), index.intValue()));
    }
    index = (Integer)RMApplyStatusEnum.PRIMARY.value();
    if (map.get(index) != null) {
      returnMap.put(index, queryApplyPsndocFilterPhs(context.getPk_org(), (FromWhereSQL)map.get(index), index.intValue()));
    }
    index = (Integer)RMApplyStatusEnum.INTERVIEW.value();
    if (map.get(index) != null) {
      AggInterviewVO[] vos = ((IInterviewQueryService)NCLocator.getInstance().lookup(IInterviewQueryService.class)).queryInterviewByFromWhereSQL(context, (FromWhereSQL)map.get(index), true);
      returnMap.put(index, vos);
    }

    index = (Integer)RMApplyStatusEnum.HIRE.value();
    if (map.get(index) != null) {
      AggRMPsndocVO[] vos = ((IHireQueryService)NCLocator.getInstance().lookup(IHireQueryService.class)).queryPsnHireInfoByFromWhereSQL(context, (FromWhereSQL)map.get(index));
      returnMap.put(index, vos);
    }

    index = (Integer)RMApplyStatusEnum.CHECKIN.value();
    if (map.get(index) != null) {
      AggCheckinVO[] vos = ((ICheckinQueryService)NCLocator.getInstance().lookup(ICheckinQueryService.class)).queryCheckinByFromWhereSQL(context, (FromWhereSQL)map.get(index));
      returnMap.put(index, vos);
    }
    return returnMap;
  }

  protected AggRMPsndocVO[] queryApplyPsndocFilterPhs(String pk_org, FromWhereSQL fromWhereSQL, int status)
    throws BusinessException
  {
    String alias = FromWhereSQLUtils.getMainTableAlias(fromWhereSQL, RMPsndocVO.getDefaultTableName());
    String normalSQL = alias + ".pk_psndoc in (select pk_psndoc from rm_psndoc_job where pk_org = '" + 
      pk_org + "' and applystatus = " + status + " and ispsnlib = 'N')";

    if ((fromWhereSQL != null) && (fromWhereSQL.getWhere() != null)) {
      String sql = FromWhereSQLUtils.createSelectSQL(fromWhereSQL, RMPsndocVO.getDefaultTableName(), new String[] { "pk_psndoc" }, null, null, null, null);
      normalSQL = normalSQL + " and " + alias + "." + "pk_psndoc" + " in ( " + sql + " ) ";
    }

    String[] fields = new RMPsndocVO().getAttributeNames();
    List fieldList = new ArrayList();
    for (String field : fields) {
      if ("photo".equals(field)) {
        continue;
      }
      fieldList.add(field);
    }
    NCObject[] retVOs = ((IMDPersistenceQueryService)NCLocator.getInstance().lookup(IMDPersistenceQueryService.class))
      .queryBillOfNCObjectByCond(AggRMPsndocVO.class, normalSQL, (String[])fieldList.toArray(new String[0]), false);
    if (ArrayUtils.isEmpty(retVOs)) {
      return null;
    }
    AggRMPsndocVO[] aggVOs = new AggRMPsndocVO[retVOs.length];
    for (int i = 0; i < retVOs.length; i++) {
      aggVOs[i] = ((AggRMPsndocVO)retVOs[i].getContainmentObject());
    }
    return processResult(pk_org, status, false, aggVOs);
  }

  protected AggRMPsndocVO[] queryApplyPsndocs(String pk_org, FromWhereSQL fromWhereSQL, int status)
    throws BusinessException
  {
    String alias = FromWhereSQLUtils.getMainTableAlias(fromWhereSQL, RMPsndocVO.getDefaultTableName());
    String normalSQL = alias + ".pk_psndoc in (select pk_psndoc from rm_psndoc_job where pk_org = '" + 
      pk_org + "' and applystatus = " + status + " and ispsnlib = 'N')";

    if ((fromWhereSQL != null) && (fromWhereSQL.getWhere() != null)) {
      String sql = FromWhereSQLUtils.createSelectSQL(fromWhereSQL, RMPsndocVO.getDefaultTableName(), new String[] { "pk_psndoc" }, null, null, null, null);
      normalSQL = normalSQL + " and " + alias + "." + "pk_psndoc" + " in ( " + sql + " ) ";
    }
    return processResult(pk_org, status, false, (AggRMPsndocVO[])getServiceTemplate().queryByCondition(AggRMPsndocVO.class, normalSQL));
  }

  public AggRMPsndocVO queryByPK(String pk_psndoc) throws BusinessException
  {
    return (AggRMPsndocVO)getServiceTemplate().queryByPk(AggRMPsndocVO.class, pk_psndoc);
  }

  public AggRMPsndocVO[] queryReApplyPsns(String pk_org)
    throws BusinessException
  {
    String normalSQL = "applytype=" + RMApplyTypeEnum.REAPPLY.toIntValue() + 
      " and pk_psndoc in (select pk_psndoc from rm_psndoc_job where pk_org = '" + pk_org + 
      "' and applystatus = " + RMApplyStatusEnum.INIT.toIntValue() + " and pk_reg_job <> '" + "0000Z700000000000000" + "') ";
    return processResult(pk_org, RMApplyStatusEnum.INIT.toIntValue(), false, (AggRMPsndocVO[])getServiceTemplate().queryByCondition(AggRMPsndocVO.class, normalSQL));
  }

  public AggRMPsndocVO[] queryRecommendPsns(String pk_psndoc, FromWhereSQL fromWhereSQL)
    throws BusinessException
  {
    String alias = FromWhereSQLUtils.getMainTableAlias(fromWhereSQL, RMPsndocVO.getDefaultTableName());
    String normalSQL = alias + "." + "pk_psndoc" + " in(select rm_psndoc_job.pk_psndoc from rm_psndoc_job where recommender='" + pk_psndoc + "')";

    if ((fromWhereSQL != null) && (fromWhereSQL.getWhere() != null)) {
      String sql = FromWhereSQLUtils.createSelectSQL(fromWhereSQL, RMPsndocVO.getDefaultTableName(), new String[] { "pk_psndoc" }, null, null, null, null);
      normalSQL = normalSQL + " and " + alias + "." + "pk_psndoc" + " in ( " + sql + " ) ";
    }
    return filterJobs(processResult(null, -1, false, (AggRMPsndocVO[])getServiceTemplate().queryByCondition(AggRMPsndocVO.class, normalSQL)));
  }
  public AggRMPsndocVO[] filterJobs(AggRMPsndocVO[] aggvos) {
    if (ArrayUtils.isEmpty(aggvos))
      return aggvos;
    for (int i = 0; i < aggvos.length; i++) {
      AggRMPsndocVO aggvo = aggvos[i];
      RMPsnJobVO[] jobVOs = (RMPsnJobVO[])aggvo.getTableVO("rm_psndoc_job");
      if (ArrayUtils.isEmpty(jobVOs))
        continue;
      List jobList = new ArrayList();
      for (int j = 0; j < jobVOs.length; j++) {
        if (ResumeSourceEnum.RECOMMEND.toIntValue() == jobVOs[j].getSourcetype().intValue())
          jobList.add(jobVOs[j]);
      }
      aggvo.setTableVO("rm_psndoc_job", CollectionUtils.isEmpty(jobList) ? null : (RMPsnJobVO[])jobList.toArray(new RMPsnJobVO[0]));
    }
    return aggvos;
  }

  public void validateApplyStatus(String[] pk_psndocs)
    throws BusinessException
  {
    new RMPsndocDAO().validateApplyStatus(pk_psndocs);
  }

  public PsnJobVO[] queryPsnJobVOs(AggRMPsndocVO aggvo)
    throws BusinessException
  {
    RMPsndocVO headVO = aggvo.getPsndocVO();
    IPsndocQryService psndocService = (IPsndocQryService)NCLocator.getInstance().lookup(IPsndocQryService.class);

    PsndocAggVO hiPsnAggVO = psndocService.queryPsndocByNameID(headVO.getName(), headVO.getIdtype(), headVO.getId());
    if ((hiPsnAggVO == null) || (ArrayUtils.isEmpty(hiPsnAggVO.getTableVO(PsnJobVO.getDefaultTableName()))))
      return null;
    PsnJobVO[] jobVOs = (PsnJobVO[])hiPsnAggVO.getTableVO(PsnJobVO.getDefaultTableName());

    List resultList = new ArrayList();
    for (PsnJobVO jobVO : jobVOs) {
      if (TrnseventEnum.DISMISSION.toIntValue() == jobVO.getTrnsevent().intValue())
        continue;
      resultList.add(jobVO);
    }
    return CollectionUtils.isEmpty(resultList) ? null : (PsnJobVO[])resultList.toArray(new PsnJobVO[0]);
  }

  public Map<String, RMPsnCPVO[]> queryPsnCPVOByJobPks(String[] pk_publishjobs)
    throws BusinessException
  {
    IPublishQueryService publishService = (IPublishQueryService)NCLocator.getInstance().lookup(IPublishQueryService.class);

    AggPublishVO publishVO = publishService.queryByPk(pk_publishjobs[0]);
    String pk_group = ((PublishJobVO)publishVO.getParentVO()).getPk_group();
    String pk_org = ((PublishJobVO)publishVO.getParentVO()).getPk_org();

    Map jobMap = publishService.queryByPk(pk_publishjobs);
    if (MapUtils.isEmpty(jobMap))
      return null;
    Map resultMap = new HashMap();
    UFLiteralDate date = PubEnv.getServerLiteralDate();
    for (String pk_publishjob : pk_publishjobs) {
      AggRMJobVO jobVO = (AggRMJobVO)jobMap.get(pk_publishjob);
      if ((jobVO == null) || (ArrayUtils.isEmpty(jobVO.getRMCPModel())))
        continue;
      RMCPModelVO[] cpmodels = jobVO.getRMCPModel();
      List psncpList = new ArrayList();
      for (RMCPModelVO cpmodel : cpmodels) {
        RMPsnCPVO vo = new RMPsnCPVO();
        vo.setPk_job(pk_publishjob);
        vo.setPk_group(pk_group);
        vo.setPk_org(pk_org);
        vo.setPk_indi(cpmodel.getPk_indi());
        vo.setPk_gradeneed(cpmodel.getPk_grade());
        vo.setWeight(cpmodel.getWeight());
        vo.setEvadate(date);
        psncpList.add(vo);
      }
      resultMap.put(pk_publishjob, (RMPsnCPVO[])psncpList.toArray(new RMPsnCPVO[0]));
    }
    return MapUtils.isEmpty(resultMap) ? null : resultMap;
  }

  private GeneralVO sysGeneralVO(RMPsnCPVO cpvo) throws BusinessException
  {
    GeneralVO genVO = new GeneralVO();

    String sql = "select hr_indi_cppe.indiname as indiname,hr_indi_cppe.scorestandard as scorestandard , hr_indi_type.inditypename as inditype from hr_indi_cppe left join hr_indi_type  on hr_indi_cppe.pk_indi_type = hr_indi_type.pk_indi_type  where pk_indi_cppe = '" + 
      cpvo.getPk_indi() + "'";
    GeneralVO[] genvos = (GeneralVO[])new BaseDAO().executeQuery(sql.toString(), new GeneralVOProcessor(GeneralVO.class));
    genVO = genvos == null ? genVO : genvos[0];
    UFDouble weight = cpvo.getWeight();
    genVO.setAttributeValue("weight", weight);
    if (weight != null) {
      DecimalFormat format = new DecimalFormat("0.00");
      genVO.setAttributeValue("weight", format.format(weight));
    }
    genVO.setAttributeValue("evadate", cpvo.getEvadate());
    genVO.setAttributeValue("remark", cpvo.getRemark());
    String gradeSql = "select pk_indi_grade,gradeseq,name from hr_indi_grade left outer join bd_defdoc on  hr_indi_grade.pk_grade = bd_defdoc.pk_defdoc  where pk_indi_grade in ('" + 
      cpvo.getPk_gradeneed() + "','" + cpvo.getPk_gradereach() + "')";
    GeneralVO[] gradevos = (GeneralVO[])new BaseDAO().executeQuery(gradeSql.toString(), new GeneralVOProcessor(GeneralVO.class));
    if (!ArrayUtils.isEmpty(gradevos)) {
      for (int i = 0; i < gradevos.length; i++)
      {
        if ((!StringUtils.isEmpty(cpvo.getPk_gradeneed())) && (cpvo.getPk_gradeneed().equals(gradevos[i].getAttributeValue("pk_indi_grade")))) {
          genVO.setAttributeValue("needgrade", gradevos[i].getAttributeValue("name"));
          genVO.setAttributeValue("needscore", (Integer)gradevos[i].getAttributeValue("gradeseq"));
        }
        if ((!StringUtils.isEmpty(cpvo.getPk_gradereach())) && (cpvo.getPk_gradereach().equals(gradevos[i].getAttributeValue("pk_indi_grade")))) {
          genVO.setAttributeValue("reachgrade", gradevos[i].getAttributeValue("name"));
          genVO.setAttributeValue("reachscore", gradevos[i].getAttributeValue("gradeseq"));
        }
      }
    }
    return genVO;
  }
  private GeneralVO sysParentVO(RMPsnJobVO jobvo, UFDouble matchResult) throws BusinessException {
    GeneralVO parentVO = new GeneralVO();

    String deptSql = "select name from org_dept where pk_dept = '" + jobvo.getPk_reg_dept() + "'";
    GeneralVO[] deptvos = (GeneralVO[])new BaseDAO().executeQuery(deptSql.toString(), new GeneralVOProcessor(GeneralVO.class));
    parentVO.setAttributeValue("deptname", deptvos[0].getAttributeValue("name"));
    parentVO.setAttributeValue("matchpercent", Integer.valueOf(matchResult.multiply(100.0D).intValue()));
    String matchSql = "select " + SQLHelper.getMultiLangNameColumn("hrcp_match_rule.rulename") + " as rulename from hrcp_match_rule where maxvalue>=" + matchResult.multiply(100.0D) + " and minvalue<=" + matchResult.multiply(100.0D);
    GeneralVO[] matchvos = (GeneralVO[])new BaseDAO().executeQuery(matchSql.toString(), new GeneralVOProcessor(GeneralVO.class));
    if (ArrayUtils.isEmpty(matchvos)) {
      String ruleSql = "select " + SQLHelper.getMultiLangNameColumn("hrcp_match_rule.rulename") + " as rulename from hrcp_match_rule where rulelevel=3";
      GeneralVO[] ruleVOs = (GeneralVO[])new BaseDAO().executeQuery(ruleSql.toString(), new GeneralVOProcessor(GeneralVO.class));
      parentVO.setAttributeValue("matchresult", ruleVOs[0].getAttributeValue("rulename"));
      return parentVO;
    }
    parentVO.setAttributeValue("matchresult", matchvos[0].getAttributeValue("rulename"));
    return parentVO;
  }

  public GeneralVO[] queryMatchResultByPsn(AggRMPsndocVO aggvo)
    throws BusinessException
  {
    if (!PubEnv.isModuleStarted(PubEnv.getPk_group(), "6004"))
      throw new BusinessException(ResHelper.getString("6021psndoc", "06021psndoc0079"));
    if (aggvo == null)
      return null;
    Integer matchType = SysInitQuery.getParaInt(PubEnv.getPk_group(), "HRCP0002");
    return matchRes(aggvo, matchType);
  }
  private GeneralVO[] matchRes(AggRMPsndocVO aggvo, Integer matchType) throws BusinessException {
    RMPsnCPVO[] cpvos = (RMPsnCPVO[])aggvo.getTableVO("rm_psndoc_cp");
    RMPsnJobVO[] jobvos = (RMPsnJobVO[])aggvo.getTableVO("rm_psndoc_job");
    List genList = new ArrayList();
    if (ArrayUtils.isEmpty(jobvos))
      return null;
    if (ArrayUtils.isEmpty(cpvos))
      return null;
    String[] pk_publishjobs = StringPiecer.getStrArray(jobvos, "pk_reg_job");
    Map aggRmjobs = ((IPublishQueryService)NCLocator.getInstance().lookup(IPublishQueryService.class)).queryByPk(pk_publishjobs);
    for (int i = 0; i < jobvos.length; i++) {
      GeneralVO[] genVOs = new GeneralVO[cpvos.length];
      UFDouble matchResult = new UFDouble();
      UFDouble matchNeed = new UFDouble();
      UFDouble matchReach = new UFDouble();
      UFDouble totalWeights = new UFDouble();

      for (int j = 0; j < cpvos.length; j++) {
        if (jobvos[i].getPk_reg_job().equals(cpvos[j].getPk_job())) {
          GeneralVO genVO = sysGeneralVO(cpvos[j]);
          int reachScore = 0;
          if (genVO.getAttributeValue("reachscore") != null) {
            reachScore = ((Integer)genVO.getAttributeValue("reachscore")).intValue();
          }
          int needScore = 0;
          if (genVO.getAttributeValue("needscore") != null) {
            needScore = ((Integer)genVO.getAttributeValue("needscore")).intValue();
          }

          if (reachScore >= needScore) {
            matchResult = matchResult.add(cpvos[j].getWeight());
            matchReach = matchReach.add(cpvos[j].getWeight().multiply(needScore));
            if (reachScore > needScore)
              genVO.setAttributeValue("matchResult", ResHelper.getString("6021psndoc", "06021psndoc0080"));
            else
              genVO.setAttributeValue("matchResult", ResHelper.getString("6021psndoc", "06021psndoc0081"));
          } else {
            genVO.setAttributeValue("matchResult", ResHelper.getString("6021psndoc", "06021psndoc0082"));
            matchReach = matchReach.add(cpvos[j].getWeight().multiply(reachScore));
          }
          totalWeights = totalWeights.add(cpvos[j].getWeight());

          matchNeed = matchNeed.add(cpvos[j].getWeight().multiply(needScore));
          genVO.setAttributeValue("jobname", MultiLangHelper.getName((RMJobVO)((AggRMJobVO)aggRmjobs.get(jobvos[i].getPk_reg_job())).getParentVO()));
          genVO.setAttributeValue("pk_job", ((RMJobVO)((AggRMJobVO)aggRmjobs.get(jobvos[i].getPk_reg_job())).getParentVO()).getPk_job());
          genVOs[j] = genVO;
        }
      }
      if (matchType.intValue() == 1) {
        if (matchReach.toDouble().doubleValue() == 0.0D)
          matchResult = matchNeed.multiply(0.0D);
        else
          matchResult = matchReach.div(matchNeed, 2, 4);
      }
      else matchResult = matchResult.div(totalWeights, 2, 4);

      GeneralVO parentVO = sysParentVO(jobvos[i], matchResult);
      parentVO.setAttributeValue("pk_job", ((RMJobVO)((AggRMJobVO)aggRmjobs.get(jobvos[i].getPk_reg_job())).getParentVO()).getPk_job());
      parentVO.setAttributeValue("jobname", MultiLangHelper.getName((RMJobVO)((AggRMJobVO)aggRmjobs.get(jobvos[i].getPk_reg_job())).getParentVO()));
      parentVO.setAttributeValue("psnname", aggvo.getPsndocVO().getMultilangName());
      parentVO.setAttributeValue("indimessage", genVOs);
      genList.add(parentVO);
    }
    if (genList.size() == 0)
      return null;
    return (GeneralVO[])genList.toArray(new GeneralVO[0]);
  }

  public void validatePsnJob(String pk_psndoc) throws BusinessException
  {
    new RMPsndocDAO().validatePsnJob(pk_psndoc);
  }

  public AggRMPsndocVO insertPsndocWithoutCheck(AggRMPsndocVO vo)
    throws BusinessException
  {
    DefaultValidationService vService = new DefaultValidationService();
    vService.addValidator(new RMPsndocDateValidator());
    vService.addValidator(new RMPsndocSameJobValidator());
    vService.validate(vo);

    RMPsndocVO headVO = vo.getPsndocVO();
    headVO.setUpdatetime(PubEnv.getServerTime());
    IPsndocQryService psndocQryService = (IPsndocQryService)NCLocator.getInstance().lookup(IPsndocQryService.class);

    PsndocAggVO hiPsnAggVO = psndocQryService.queryPsndocByNameID(headVO.getName(), headVO.getIdtype(), headVO.getId());

    if (hiPsnAggVO != null) {
      headVO.setApplytype(Integer.valueOf(RMApplyTypeEnum.REAPPLY.toIntValue()));
      headVO.setApplystatus(Integer.valueOf(RMApplyStatusEnum.APPLY.toIntValue()));
      headVO.setPk_hipsndoc(hiPsnAggVO.getParentVO().getPk_psndoc());
    }

    setEduInfoForPsndoc(vo);

    return directInsert(vo);
  }

  private void setEduInfoForPsndoc(AggRMPsndocVO vo)
  {
    if (vo == null) {
      return;
    }
    RMPsndocVO headVO = vo.getPsndocVO();
    RMEduVO[] eduVOs = (RMEduVO[])vo.getTableVO(RMEduVO.getDefaultTableName());
    if (ArrayUtils.isEmpty(eduVOs)) {
      return;
    }
    for (RMEduVO eduVO : eduVOs)
      if (new UFBoolean("Y").equals(eduVO.getLastflag())) {
        headVO.setEdu(eduVO.getEducation());
        headVO.setPk_degree(eduVO.getDegree());
        break;
      }
  }

  public Object updateForPort(AggRMPsndocVO vo)
    throws BusinessException
  {
    String checkResult = checkUniqueInfo(vo.getPsndocVO());
    if (checkResult != null) {
      return checkResult;
    }
    DefaultValidationService vService = new DefaultValidationService();
    vService.addValidator(new RMPsndocDateValidator());
    vService.addValidator(new RMPsndocSameJobValidator());
    vService.validate(vo);

    RMPsndocVO headVO = vo.getPsndocVO();
    headVO.setUpdatetime(PubEnv.getServerTime());
    IPsndocQryService psndocQryService = (IPsndocQryService)NCLocator.getInstance().lookup(IPsndocQryService.class);

    PsndocAggVO hiPsnAggVO = psndocQryService.queryPsndocByNameID(headVO.getName(), headVO.getIdtype(), headVO.getId());

    if (hiPsnAggVO != null) {
      headVO.setApplytype(Integer.valueOf(RMApplyTypeEnum.REAPPLY.toIntValue()));
      headVO.setPk_hipsndoc(hiPsnAggVO.getParentVO().getPk_psndoc());
    }
    return directUpdate(vo);
  }

  public void reqTrans(String trnsType, RMPsnJobVO[] jobVOs)
    throws BusinessException
  {
    if (ArrayUtils.isEmpty(jobVOs))
      return;
    String[] pks = StringPiecer.getStrArray(jobVOs, "pk_psndoc");
    AggRMPsndocVO[] aggvos = ((IRMPsndocQueryService)NCLocator.getInstance().lookup(IRMPsndocQueryService.class)).queryPsndocByPks(pks);
    if (ArrayUtils.isEmpty(aggvos)) {
      return;
    }
    Map jobMap = new HashMap();
    for (RMPsnJobVO jobVO : jobVOs) {
      jobMap.put(jobVO.getPk_psndoc(), jobVO);
    }
    ITransmngManageService itm = (ITransmngManageService)NCLocator.getInstance().lookup(ITransmngManageService.class);
    Object unUsePk = new ArrayList();
    for (int i = 0; i < aggvos.length; i++) {
      tranPsnVO = new RM2TRNLinkData();
      tranPsnVO.setPk_org(((RMPsnJobVO)jobMap.get(aggvos[i].getPsndocVO().getPk_psndoc())).getPk_reg_org());
      if (StringUtils.isEmpty(aggvos[i].getPsndocVO().getPk_hipsndoc()))
        continue;
      tranPsnVO.setPk_psndoc(aggvos[i].getPsndocVO().getPk_hipsndoc());
      tranPsnVO.setPk_trnstype(trnsType);
      tranPsnVO.setPk_dept(((RMPsnJobVO)jobMap.get(aggvos[i].getPsndocVO().getPk_psndoc())).getPk_reg_dept());

      itm.createCrossInBill4RM(tranPsnVO);

      RMPsnJobVO[] psnJobVOs = (RMPsnJobVO[])aggvos[i].getTableVO(RMPsnJobVO.getDefaultTableName());
      for (RMPsnJobVO psnJobVO : psnJobVOs) {
        if (psnJobVO.getPk_psndoc_job().equals(((RMPsnJobVO)jobMap.get(aggvos[i].getPsndocVO().getPk_psndoc())).getPk_psndoc_job()))
          continue;
        ((List)unUsePk).add(psnJobVO.getPk_psndoc_job());
      }
    }
    InSQLCreator isc = new InSQLCreator();
    try {
      new RMPsndocDAO().updateApplyStatusByCond(RMApplyStatusEnum.INDOC.toIntValue(), "pk_psndoc_job in (" + isc.getInSQL(jobVOs, "pk_psndoc_job") + ")");
    } finally {
      isc.clear();
    }

    if (CollectionUtils.isEmpty((Collection)unUsePk))
      return;
    ((IRMPsndocManageService)NCLocator.getInstance().lookup(IRMPsndocManageService.class)).unUseJobsBypks((String[])((List)unUsePk).toArray(new String[0]));
  }

  public AggRMPsndocVO queryByPK(String pk_org, String pk_psndoc)
    throws BusinessException
  {
    AggRMPsndocVO[] aggVOs = processResult(pk_org, -1, true, new AggRMPsndocVO[] { queryByPK(pk_psndoc) });
    if (ArrayUtils.isEmpty(aggVOs))
      return null;
    return aggVOs[0];
  }
}