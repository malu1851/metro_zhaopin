package nc.vo.hrss.pub;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nc.uap.lfw.core.base.ExtendAttributeSupport;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hi.psndoc.PsndocVO;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.om.hrdept.HRDeptVO;
import nc.vo.sm.UserVO;
import nc.vo.sm.funcreg.FuncRegisterVO;
import nc.vo.uif2.LoginContext;

public class SessionBean
  extends ExtendAttributeSupport
  implements Serializable
{
  private static final long serialVersionUID = -5322062171738826621L;
  private UserVO userVO = null;
  private RmUserVO rmUserVO = null;
  private int brower_jobtyle = 0;
  private int type =0;
  private PsndocVO psndocVO = null;
  private PsnJobVO psnjobVO = null;
  private List<PsnJobVO> psnjobVOs = null;
  private Map<String, FuncRegisterVO> funcRegisterVOs = null;
  private String pk_mng_group = null;
  private String pk_mng_org = null;
  private String pk_mng_dept = null;
  private boolean includeSubDept = false;
  private String mng_dept_code = null;
  private HRDeptVO[] mngDeptVOs = null;
  private String psnScopeSqlPart;
  private LoginContext context = null;
  private boolean isTrmer = false;
  
  public SessionBean() {}
  
  
  
  public int getType() {
	return type;
}



public void setType(int type) {
	this.type = type;
}



public boolean isTrmer()
  {
    return this.isTrmer;
  }
  
  public void setTrmer(boolean isTrmer)
  {
    this.isTrmer = isTrmer;
  }
  
  private HashMap<String, ArrayList<String>> openedFuncList = null;
  
  public int getBrower_jobtyle()
  {
    return this.brower_jobtyle;
  }
  
  public void setBrower_jobtyle(int brower_jobtyle)
  {
    this.brower_jobtyle = brower_jobtyle;
  }
  
  public UserVO getUserVO()
  {
    return this.userVO;
  }
  
  public void setUserVO(UserVO userVO)
  {
    this.userVO = userVO;
  }
  
  public PsndocVO getPsndocVO()
  {
    return this.psndocVO;
  }
  
  public void setPsndocVO(PsndocVO psndocVO)
  {
    this.psndocVO = psndocVO;
  }
  
  public PsnJobVO getPsnjobVO()
  {
    return this.psnjobVO;
  }
  
  public void setPsnjobVO(PsnJobVO psnjobVO)
  {
    this.psnjobVO = psnjobVO;
  }
  
  public List<PsnJobVO> getPsnjobVOs()
  {
    return this.psnjobVOs;
  }
  
  public void setPsnjobVOs(List<PsnJobVO> psnjobVOs)
  {
    this.psnjobVOs = psnjobVOs;
  }
  
  public Map<String, FuncRegisterVO> getFuncRegisterVOs()
  {
    return this.funcRegisterVOs;
  }
  
  public void setFuncRegisterVOs(Map<String, FuncRegisterVO> funcRegisterVOs)
  {
    this.funcRegisterVOs = funcRegisterVOs;
  }
  
  public String getPk_org()
  {
    return null == getPsnjobVO() ? null : getPsnjobVO().getPk_org();
  }
  
  public String getPk_group()
  {
    return null == getPsnjobVO() ? null : getPsnjobVO().getPk_group();
  }
  
  public String getPk_dept()
  {
    return null == getPsnjobVO() ? null : getPsnjobVO().getPk_dept();
  }
  
  public String getPk_mng_group()
  {
    return this.pk_mng_group;
  }
  
  public void setPk_mng_group(String pk_mng_group)
  {
    this.pk_mng_group = pk_mng_group;
  }
  
  public String getPk_mng_org()
  {
    return this.pk_mng_org;
  }
  
  public void setPk_mng_org(String pk_mng_org)
  {
    this.pk_mng_org = pk_mng_org;
  }
  
  public String getPk_mng_dept()
  {
    return this.pk_mng_dept;
  }
  
  public void setPk_mng_dept(String pk_mng_dept)
  {
    this.pk_mng_dept = pk_mng_dept;
  }
  
  public String getMng_dept_code()
  {
    return this.mng_dept_code;
  }
  
  public void setMng_dept_code(String mng_dept_code)
  {
    this.mng_dept_code = mng_dept_code;
  }
  
  public HRDeptVO[] getMngDeptVOs()
  {
    return this.mngDeptVOs;
  }
  
  public void setMngDeptVOs(HRDeptVO[] mngDeptVOs)
  {
    this.mngDeptVOs = mngDeptVOs;
  }
  
  public LoginContext getContext()
  {
    return this.context;
  }
  
  public void setContext(LoginContext context)
  {
    this.context = context;
  }
  
  public HashMap<String, ArrayList<String>> getOpenedFuncList()
  {
    if (null == this.openedFuncList) {
      this.openedFuncList = new HashMap();
    }
    return this.openedFuncList;
  }
  
  public void setOpenedFuncList(HashMap<String, ArrayList<String>> openedFuncList)
  {
    this.openedFuncList = openedFuncList;
  }
  
  public boolean isIncludeSubDept()
  {
    return this.includeSubDept;
  }
  
  public void setIncludeSubDept(boolean includeSubDept)
  {
    this.includeSubDept = includeSubDept;
  }
  
  public RmUserVO getRmUserVO()
  {
    return this.rmUserVO;
  }
  
  public void setRmUserVO(RmUserVO rmUserVO)
  {
    this.rmUserVO = rmUserVO;
  }
  
  public String getPsnScopeSqlPart()
  {
    return this.psnScopeSqlPart;
  }
  
  public void setPsnScopeSqlPart(String psnScopeSqlPart)
  {
    this.psnScopeSqlPart = psnScopeSqlPart;
  }
}
