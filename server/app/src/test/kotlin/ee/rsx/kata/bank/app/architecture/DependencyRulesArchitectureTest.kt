package ee.rsx.kata.bank.app.architecture

import com.tngtech.archunit.base.DescribedPredicate.not
import com.tngtech.archunit.core.domain.JavaClass.Predicates.ENUMS
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.DisplayName

@AnalyzeClasses(packages = ["ee.rsx.kata.bank"])
@Suppress("unused")
@DisplayName("Dependency Rules of architecture")
internal class DependencyRulesArchitectureTest {

  @ArchTest
  private val `Classes outside of Core (except gateway adapters) should not depend on classes in core` =
    noClasses().that()
      .resideInAPackage("$BANK_ROOT_PACKAGE..")
      .and()
      .resideOutsideOfPackages(*BANK_CORE_PACKAGES)
      .and(
        ignoreGatewayAdapterClasses()
      )
      .should()
      .dependOnClassesThat(
        resideInAnyPackage(*BANK_CORE_PACKAGES)
      )

  @ArchTest
  private val `Bank Core classes should not depend on Spring Framework` =
    noClasses().that()
      .resideInAnyPackage(*BANK_CORE_PACKAGES)
      .should()
      .dependOnClassesThat().resideInAPackage("org.springframework..")

  @ArchTest
  private val `Domain classes should not depend on any bank loans classes outside of domain` =
    noClasses().that()
      .resideInAPackage(
        "$BANK_ROOT_PACKAGE.(**).domain.."
      )
      .should()
      .dependOnClassesThat(
        areBankLoansClassesOutsideOfDomain()
      )

  private fun areBankLoansClassesOutsideOfDomain() =
    resideInAPackage("$BANK_ROOT_PACKAGE..")
      .and(resideOutsideOfPackage("..domain.."))
      .and(not(ENUMS))

  companion object {
    const val BANK_ROOT_PACKAGE = "ee.rsx.kata.bank"

    val BANK_CORE_PACKAGES = listOf(
      "$BANK_ROOT_PACKAGE.(**).domain..",
      "$BANK_ROOT_PACKAGE.(**).usecases.."
    )
      .toTypedArray<String>()

    private fun ignoreGatewayAdapterClasses() =
      not(resideInAPackage("$BANK_ROOT_PACKAGE.(**).adapter.."))
  }
}
