package urldsl.language

import urldsl.errors.SimpleFragmentMatchingError

final class FragmentSimpleErrorProperties
    extends FragmentProperties[SimpleFragmentMatchingError](
      Fragment.simpleFragmentErrorImpl,
      SimpleFragmentMatchingError.itIsFragmentMatchingError,
      "FragmentSimpleErrorProperties"
    )
