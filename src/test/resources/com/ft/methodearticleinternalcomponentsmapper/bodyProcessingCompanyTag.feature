@BodyProcessing
Feature: Body processing Company Tags

  Scenario Outline:Company tags are processed for concorded Composite ids
   Given There is a company tag in a Methode article body
   Then it is transformed, <before> becomes <after>

  Examples:
    | before                                                         | after                                                         |
    # Text within the a tag is preserved:
    | <p><company DICoName="Citigroup Inc" DICoFTMWTickercode="us:C"  DICoSEDOL="2297907"  DICoTickerSymbol="C" DICoTickerExchangeCode="" CompositeId="TnN0ZWluX09OX0ZvcnR1bmVDb21wYW55X0M=-T04=">Citigroup</company> has been picked to safeguard $230bn worth of securities.</p> | <p><ft-concept type="http://www.ft.com/ontology/company/PublicCompany" id="704a3225-9b5c-3b4f-93c7-8e6a6993bfb0">Citigroup</ft-concept> has been picked to safeguard $230bn worth of securities.</p>               |
   
  Scenario Outline: Company tags are stripped for missing Composite ids
    Given the Methode body has <tagname> the transformer will STRIP ELEMENT AND LEAVE CONTENT BY DEFAULT

  Examples: Remove tag but leave any content - these are just some examples, by default anything not specified separately will be treated like this
    | tagname  | html                                           |
    | company  | <p><company DICoName="Citigroup Inc" DICoFTMWTickercode="us:C"  DICoSEDOL="2297907"  DICoTickerSymbol="C" DICoTickerExchangeCode=">Citigroup</company></p>|
    
  Scenario Outline: Company tags are stripped for un-concorded Composite ids
    Given the Methode body has <tagname> the transformer will STRIP ELEMENT AND LEAVE CONTENT BY DEFAULT

  Examples: Remove tag but leave any content - these are just some examples, by default anything not specified separately will be treated like this
    | tagname   | html                                           |
    | company   | <p><company DICoName="Citigroup Inc" DICoFTMWTickercode="us:C"  DICoSEDOL="2297907"  DICoTickerSymbol="C" DICoTickerExchangeCode="" CompositeId="notconcorded">Citigroup</company> has been picked to safeguard $230bn worth of securities.</p>                              |

  Scenario Outline:Company tags are processed for a mixture of concorded and uncorncorded Composite ids
    Given There is a company tag in a Methode article body
    When it is transformed, <before> becomes <after>

  Examples:
     | before                                                         | after                                                         |
    # Text within the a tag is preserved:
    | <p><company DICoName="Citigroup Inc" DICoFTMWTickercode="us:C" CompositeId="notconcorded">Not Concorded Company</company> more text here<company DICoName="Citigroup Inc" DICoFTMWTickercode="us:C" DICoTickerExchangeCode="" CompositeId="TnN0ZWluX09OX0ZvcnR1bmVDb21wYW55X0M=-T04=">Citigroup</company> has been picked to safeguard $230bn worth of securities.</p> |<p>Not Concorded Company more text here<ft-concept type="http://www.ft.com/ontology/company/PublicCompany" id="704a3225-9b5c-3b4f-93c7-8e6a6993bfb0">Citigroup</ft-concept> has been picked to safeguard $230bn worth of securities.</p>               |
   

  Scenario Outline: Company tags are processed wherever they are in the story body xpath
    Given  There is a company tag in a Methode article body
    When it is transformed, <before> becomes <after>

  Examples:
    | before                                                         | after                                                         |
    # Text within the a tag is preserved:
    | <b><company DICoName="Citigroup Inc" DICoFTMWTickercode="us:C"  DICoSEDOL="2297907"  DICoTickerSymbol="C" DICoTickerExchangeCode="" CompositeId="TnN0ZWluX09OX0ZvcnR1bmVDb21wYW55X0M=-T04=">Citigroup</company> has been picked to safeguard $230bn worth of securities.</b> | <strong><ft-concept type="http://www.ft.com/ontology/company/PublicCompany" id="704a3225-9b5c-3b4f-93c7-8e6a6993bfb0">Citigroup</ft-concept> has been picked to safeguard $230bn worth of securities.</strong> |
    | <company DICoName="Citigroup Inc" DICoFTMWTickercode="us:C"  DICoSEDOL="2297907"  DICoTickerSymbol="C" DICoTickerExchangeCode="" CompositeId="TnN0ZWluX09OX0ZvcnR1bmVDb21wYW55X0M=-T04=">Citigroup</company> has been picked to safeguard $230bn worth of securities.        | <ft-concept type="http://www.ft.com/ontology/company/PublicCompany" id="704a3225-9b5c-3b4f-93c7-8e6a6993bfb0">Citigroup</ft-concept> has been picked to safeguard $230bn worth of securities.        |

 