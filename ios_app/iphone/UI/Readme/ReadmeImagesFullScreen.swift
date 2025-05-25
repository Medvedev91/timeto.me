import SwiftUI

struct ReadmeImagesFullScreen: View {
    
    let images: [String]
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        VStack {
            
            ScrollView(.horizontal, showsIndicators: false) {
                
                HStack {
                    
                    ForEach(images, id: \.self) { item in
                        
                        Image(item)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .cornerRadius(16)
                            .shadow(color: .primary, radius: onePx)
                            .fillMaxHeight()
                            .padding(.trailing, 12)
                            .padding(.bottom, 8) // Paddings for shadow radius
                    }
                }
                .scrollTargetLayout()
            }
            .contentMargins(.leading, 16, for: .scrollContent)
            .contentMargins(.trailing, 4, for: .scrollContent)
            .scrollTargetBehavior(.viewAligned)
        }
        .toolbar {
            
            ToolbarItem(placement: .topBarTrailing) {
                
                Button(
                    action: {
                        dismiss()
                    },
                    label: {
                        Text("Done")
                            .fontWeight(.bold)
                            .foregroundColor(.blue)
                    }
                )
            }
        }
        .gesture(
            DragGesture().onEnded { value in
                let xDiffAbs = abs(value.location.x - value.startLocation.x)
                let yDiff = value.location.y - value.startLocation.y
                if yDiff > 50, xDiffAbs < 50 {
                    dismiss()
                }
            }
        )
        .attachNavigation()
    }
}
