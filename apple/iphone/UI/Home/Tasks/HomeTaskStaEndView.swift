import SwiftUI
import shared

struct HomeTaskStaEndView: View {
    
    let onMoveToTimer: () -> Void
    let onDelete: () -> Void
    let onCancel: () -> Void
    
    var body: some View {
        HStack {
            
            Text("Move to Timer")
                .padding(.leading, 8)
                .padding(.trailing, 4)
                .foregroundColor(.white)
                .lineLimit(1)
                .onTapGesture {
                    onMoveToTimer()
                }
            
            Spacer()
            
            Button("Cancel") {
                onCancel()
            }
            .foregroundColor(.white)
            .padding(.trailing, 12)
            
            Button(
                action: {
                    onDelete()
                },
                label: {
                    Text("Delete")
                        .fontWeight(.bold)
                        .padding(.horizontal, 9)
                        .padding(.vertical, 5)
                        .foregroundColor(.red)
                }
            )
            .background(RoundedRectangle(cornerRadius: 999, style: .circular).fill(.white))
            .padding(.trailing, 8)
        }
        .fillMaxHeight()
        .background(.red)
    }
}
