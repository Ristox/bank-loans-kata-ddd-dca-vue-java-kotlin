package ee.rsx.kata.bank.app.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.List;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = {"ee.rsx.kata.bank"})
class DependencyRulesArchitectureTest {

  public static final String BANK_ROOT_PACKAGE = "ee.rsx.kata.bank";

  public static final String[] BANK_CORE_PACKAGES =
    List.of(
      BANK_ROOT_PACKAGE + ".(**).domain..",
      BANK_ROOT_PACKAGE + ".(**).usecases.."
    )
      .toArray(new String[0]);

  @ArchTest
  private final ArchRule classesOutsideOfCore_exceptGatewayAdapters_shouldNotDependOn_classesInCore =
    noClasses().that()
      .resideInAPackage(BANK_ROOT_PACKAGE + "..")
      .and()
      .resideOutsideOfPackages(BANK_CORE_PACKAGES)
      .and(
        ignoreGatewayAdapterClasses()
      )
      .should()
      .dependOnClassesThat(
        resideInAnyPackage(BANK_CORE_PACKAGES)
      );

  private static DescribedPredicate<JavaClass> ignoreGatewayAdapterClasses() {
    return not(resideInAPackage(BANK_ROOT_PACKAGE + ".(**).adapter.."));
  }

  @ArchTest
  private final ArchRule bankCoreClasses_shouldNotDependOn_springFramework =
    noClasses().that()
      .resideInAnyPackage(BANK_CORE_PACKAGES)
      .should()
      .dependOnClassesThat().resideInAPackage("org.springframework..");

  @ArchTest
  private final ArchRule domainClasses_shouldNotDependOn_anyBankLoansClasses_outsideOfDomain =
    noClasses().that()
      .resideInAPackage(
        BANK_ROOT_PACKAGE + ".(**).domain.."
        )
      .should()
      .dependOnClassesThat(
        areBankLoansClassesOutsideOfDomain()
      );

  private DescribedPredicate<JavaClass> areBankLoansClassesOutsideOfDomain() {
    return resideInAPackage(BANK_ROOT_PACKAGE + "..")
      .and(resideOutsideOfPackage("..domain.."))
      .and(not(ENUMS));
  }
}
