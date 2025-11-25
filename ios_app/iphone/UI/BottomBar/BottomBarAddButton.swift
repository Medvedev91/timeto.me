import SwiftUI

struct BottomBarAddButton: View {
    
    let text: String
    let action: () -> Void
    
    var body: some View {
        
        Button(
            action: {
                action()
            },
            label: {
                
                HStack(spacing: 8) {
                    
                    Image(systemName: "plus.circle.fill")
                        .foregroundStyle(.blue)
                        .fontWeight(.bold)
                    
                    Text(text)
                        .foregroundColor(.blue)
                        .fontWeight(.semibold)
                }
            }
        )
        .frame(maxWidth: .infinity)
    }
}
