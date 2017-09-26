@BodyProcessing
Feature: Body processing

  This shows how particular tags are processed.

  For details of how tags will look before and after for particular processing rules, see bodyprocessingrule.feature.

  Scenario Outline: Transform tag to HTML 5 equivalent
    Given the Methode body contains <tagname> the transformer will TRANSFORM THE TAG and the replacement tag will be <replacement>

  Examples: Transform tags to HTML 5 equivalent
    | tagname | replacement |
    | b       | strong      |
    | i       | em          |


  Scenario Outline: Transform the tag but as a block element
    Given the Methode body contains <tagname> the transformer will REPLACE BLOCK ELEMENT TAG and the replacement tag will be <replacement>


  Examples: Transform tags
    | tagname   | replacement   |
    | timeline  | ft-timeline   |


  Scenario Outline: Transform the inline image and wrap it in specified tags
    Given the Methode body has <tagname> the transformer will WRAP AND TRANSFORM A INLINE IMAGE

  Examples:
    | tagname         |
    | timeline-image  |


  Scenario Outline: Retain element and remove formatting attributes
    Given the Methode body has <tagname> the transformer will RETAIN ELEMENT AND REMOVE FORMATTING ATTRIBUTES

  Examples: Tidy up and transform tags to remove attributes that probably relate to formatting
    | tagname |
    | table   |

  Scenario Outline: Transform and tidy up podcast
    Given the Methode body has <tagname> the transformer will TRANSFORM THE SCRIPT ELEMENT TO PODCAST

  Examples: Tidy up and transform tags to remove attributes that probably relate to formatting
    | tagname |
    | script  |

  Scenario Outline: Transform and tidy up big number
    Given the Methode body has <tagname> the transformer will TRANSFORM TAG IF BIG NUMBER

  Examples: Tidy up and transform tags to remove attributes that probably relate to formatting
    | tagname   |
    | promo-box |

  Scenario Outline: Transform and tidy up Brightcove video
    Given the Methode body has <tagname> the transformer will TRANSFORM THE TAG TO VIDEO

  Examples: Tidy up and transform tags to remove attributes that probably relate to formatting
    | tagname     |
    | videoPlayer |

  Scenario Outline: Transform and tidy up Next video
    Given the Methode body has <tagname> the transformer will TRANSFORM THE NEXT TAG TO VIDEO

  Examples: Tidy up and transform tags to remove attributes that probably relate to formatting
    | tagname     |
    | videoPlayer |

  Scenario Outline: Transform and tidy up linked Next video
    Given the Methode body has <tagname> the transformer will TRANSFORM THE LINK TAG TO VIDEO

  Examples: Tidy up and transform tags to remove attributes that probably relate to formatting
    | tagname     |
    | content |

  Scenario Outline: Transform and tidy web pull quote
    Given the Methode body has <tagname> the transformer will TRANSFORM THE WEB-PULL-QUOTE TO PULL-QUOTE

  Examples: Tidy up and transform tags to remove attributes that probably relate to formatting
    | tagname        |
    | web-pull-quote |

  Scenario Outline: Strip element and contents
    Given the Methode body has <tagname> the transformer will STRIP ELEMENT AND CONTENTS

  Examples: Remove tags completely, including content, for html5 tags that we cannot support currently
    | tagname                    |
    | annotation                 |
    | applet                     |
    | audio                      |
    | base                       |
    | basefont                   |
    | button                     |
    | canvas                     |
    | caption                    |
    | col                        |
    | colgroup                   |
    | command                    |
    | datalist                   |
    | del                        |
    | dir                        |
    | embed                      |
    | fieldset                   |
    | form                       |
    | frame                      |
    | frameset                   |
    | head                       |
    | input                      |
    | keygen                     |
    | label                      |
    | legend                     |
    | link                       |
    | map                        |
    | menu                       |
    | meta                       |
    | nav                        |
    | noframes                   |
    | noscript                   |
    | object                     |
    | optgroup                   |
    | option                     |
    | output                     |
    | param                      |
    | progress                   |
    | rp                         |
    | rt                         |
    | ruby                       |
    | s                          |
    | select                     |
    | source                     |
    | strike                     |
    | style                      |
    | tbody                      |
    | td                         |
    | textarea                   |
    | tfoot                      |
    | th                         |
    | thead                      |
    | tr                         |
    | track                      |
    | video                      |
    | wbr                        |
    | byline                     |
    | editor-choice              |
    | headline                   |
    | inlineDwc                  |
    | interactive-chart          |
    | lead-body                  |
    | lead-text                  |
    | ln                         |
    | photo                      |
    | photo-caption              |
    | photo-group                |
    | plainHtml                  |
    | promo-headline             |
    | promo-image                |
    | promo-intro                |
    | promo-link                 |
    | promo-title                |
    | promobox-body              |
    | pull-quote-header          |
    | pull-quote-text            |
    | readthrough                |
    | short-body                 |
    | skybox-body                |
    | stories                    |
    | story                      |
    | strap                      |
    | videoObject                |
    | web-alt-picture            |
    | web-background-news        |
    | web-background-news-header |
    | web-background-news-text   |
    | web-picture                |
    | web-pull-quote-source      |
    | web-pull-quote-text        |
    | web-skybox-picture         |
    | web-subhead                |
    | web-thumbnail              |
    | xref                       |
    | xrefs                      |



  Scenario Outline:Transform and tidy up other video types
    Given the Methode body has <tagname> the transformer will TRANSFORM OTHER VIDEO TYPES

  Examples: Tidy up tags to remove attributes that probably relate to formatting
    | tagname |
    | iframe  |

  Scenario Outline: Retain element and remove attributes
    Given the Methode body has <tagname> the transformer will RETAIN ELEMENT AND REMOVE ATTRIBUTES

  Examples: Tidy up tags to remove attributes that probably relate to formatting
    | tagname |
    | strong  |
    | em      |
    | sub     |
    | sup     |
    | h1      |
    | h2      |
    | h3      |
    | h4      |
    | h5      |
    | h6      |
    | ol      |
    | ul      |
    | li      |

  Scenario Outline: Strip elements and retain content
    Given the Methode body has <tagname> the transformer will STRIP ELEMENT AND LEAVE CONTENT BY DEFAULT

  Examples: Remove tag but leave any content - these are just some examples, by default anything not specified separately will be treated like this
    | tagname   | html                                           |
    | !--       | <!-- comments -->                              |
    | weird     | <weird>text surrounded by unknown tags</weird> |


  Scenario Outline: Entity translation to unicode
    Given an entity reference <entity>
    When I transform it into our Content Store format
    Then the entity should be replace by unicode codepoint <codepoint>

  Examples:
    | entity | codepoint |
    | &euro; | 0x20AC    |
    | &nbsp; | 0x00A0    |