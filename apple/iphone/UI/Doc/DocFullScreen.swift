import SwiftUI
import shared

struct DocFullScreen: View {
    
    let forceRead: Bool
    
    var body: some View {
        VmView({
            DocVm()
        }) { vm, state in
            let state = vm.state.value as! DocVm.State
            DocFullScreenInner(
                vm: vm,
                state: state,
                forceRead: forceRead,
            )
        }
    }
}

private struct DocFullScreenInner: View {
    
    let vm: DocVm
    let state: DocVm.State
    
    let forceRead: Bool
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        List {
            
            if forceRead {
                
                PView {
                    Text("I force you to read this guide because without it, you will not understand how to use the app.")
                        .forceText()
                        .padding(.top, 4)
                }
                
                PView {
                    Text("Please DO NOT SKIP this! It will help you get started and begin improving your life.")
                        .forceText()
                }
                
                PView {
                    Text("Good luck!")
                        .forceText()
                }
                
                Divider()
                    .fillMaxWidth()
                    .frame(height: 1)
                    .background(.separator)
            }
            
            PView {
                Text("I built this app to manage my productivity. Here, I will ") +
                Text("SHARE")
                    .greenSemiBold() +
                Text(" my productivity system and how I use the app.")
            }
            
            PView {
                Text("My system ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" about time tracking, ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" about getting nice activity charts, ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" about reducing wasted time.")
            }
            
            PView {
                Text("My system ") +
                Text("IS ALL ABOUT")
                    .greenSemiBold() +
                Text(" achieving my ") +
                Text("REAL-LIFE")
                    .greenSemiBold() +
                Text(" goals.")
            }
            
            PView {
                Text("For example, ") +
                Text("I DO NOT")
                    .redSemiBold() +
                Text(" care how much time I waste, but ") +
                Text("I CARE")
                    .greenSemiBold() +
                Text(" if I read a book every day, ") +
                Text("I CARE")
                    .greenSemiBold() +
                Text(" if I exercise every day, ") +
                Text("I CARE")
                    .greenSemiBold() +
                Text(" if I don't forget anything, ") +
                Text("I CARE")
                    .greenSemiBold() +
                Text(" if I constantly follow my long-term goals.")
            }
            
            PView {
                Text("Now I will show ") +
                Text("MY PERSONAL")
                    .greenSemiBold() +
                Text(" app setup with ") +
                Text("REAL-LIFE")
                    .greenSemiBold() +
                Text(" scenarios.")
            }
            
            PView {
                Text("IMPORTANT!")
                    .blueSemiBold() +
                Text(" Life is hard, life is tricky. No way to have a perfect app or system.") +
                Text(" Some solutions seem strange, but they work, they help me achieve my ") +
                Text("REAL-LIFE")
                    .greenSemiBold() +
                Text(" goals.")
            }
            
            HeaderView("Activities")
            
            PView {
                Text("The first thing you have to do is ") +
                Text("SET UP ACTIVITIES.")
                    .greenSemiBold()
            }
            
            PView {
                Text("This is how ") +
                Text("MY ACTIVITIES")
                    .greenSemiBold() +
                Text(" look in the morning, right after I wake up:")
            }
        }
        .listStyle(.plain)
        .navigationTitle("How to Use the App")
        .toolbar {
            if !forceRead {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") {
                        dismiss()
                    }
                }
            }
        }
    }
}

private struct PView<Content: View>: View {
    
    @ViewBuilder private let content: () -> Content
    
    init(_ content: @escaping () -> Content) {
        self.content = content
    }
    
    var body: some View {
        content()
            .listRowSeparator(.hidden)
    }
}

private struct HeaderView: View {
    
    private let text: String
    
    init(_ text: String) {
        self.text = text
    }
    
    var body: some View {
        Text(text)
            .font(.system(size: 30, weight: .bold))
            .listRowSeparator(.hidden)
            .padding(.top, 32)
    }
}

private extension Text {
    
    func redSemiBold() -> Text {
        foregroundColor(.red).fontWeight(.bold)
    }
    
    func greenSemiBold() -> Text {
        foregroundColor(.green).fontWeight(.bold)
    }
    
    func blueSemiBold() -> Text {
        foregroundColor(.blue).fontWeight(.bold)
    }
    
    ///
    
    func forceText() -> Text {
        foregroundColor(.blue).font(.system(size: 20, weight: .bold))
    }
}
