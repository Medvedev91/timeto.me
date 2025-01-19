import SwiftUI

extension View {
    
    func textAlign(_ alignment: TextAlignment) -> some View {
        let frameAlignment: Alignment = switch alignment {
        case .leading: .leading
        case .center:  .center
        case .trailing: .trailing
        }
        return self
            .frame(maxWidth: .infinity, alignment: frameAlignment)
            .multilineTextAlignment(.leading)
    }
}
